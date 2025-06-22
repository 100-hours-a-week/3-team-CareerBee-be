package org.choon.careerbee.domain.competition.service.command;

import io.sentry.Sentry;
import static org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq.SubmitInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.internal.GradingResult;
import org.choon.careerbee.domain.competition.dto.internal.ProblemAnswerInfo;
import org.choon.careerbee.domain.competition.dto.internal.SubmissionContext;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.response.CompetitionGradingResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionGradingResp.CompetitionGradingInfo;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionProblemRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Slf4j
@Service
public class CompetitionCommandServiceImpl implements CompetitionCommandService {

    private static final Integer PARTICIPATION_POINT = 5;

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionResultRepository competitionResultRepository;
    private final CompetitionSummaryRepository summaryRepository;
    private final MemberQueryService memberQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final CompetitionProblemRepository competitionProblemRepository;

    @Override
    public void joinCompetition(Long competitionId, Long accessMemberId) {
        Competition validCompetition = competitionRepository.findById(competitionId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST));

        Member validMember = memberQueryService.findById(accessMemberId);

        if (competitionParticipantRepository
            .existsByMemberIdAndCompetitionId(accessMemberId, competitionId)
        ) {
            throw new CustomException(CustomResponseStatus.COMPETITION_ALREADY_JOIN);
        }

        competitionParticipantRepository.save(
            CompetitionParticipant.of(validMember, validCompetition)
        );
    }

    @Override
    public CompetitionGradingResp submitCompetitionResult(
        Long competitionId,
        CompetitionResultSubmitReq req,
        Long memberId
    ) {
        SubmissionContext context = validateSubmission(competitionId, memberId);
        GradingResult grading = gradeAnswers(competitionId, req);
        persistAndNotify(context, grading, req.elapsedTime());

        return new CompetitionGradingResp(grading.gradingInfos());
    }

    private SubmissionContext validateSubmission(Long competitionId, Long memberId) {
        Competition competition = competitionRepository.findById(competitionId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST));

        Member member = memberQueryService.findById(memberId);

        if (competitionResultRepository.existsByMemberIdAndCompetitionId(memberId, competitionId)) {
            throw new CustomException(CustomResponseStatus.RESULT_ALREADY_SUBMIT);
        }

        return new SubmissionContext(competition, member);
    }

    private GradingResult gradeAnswers(Long competitionId, CompetitionResultSubmitReq req) {
        Map<Long, ProblemAnswerInfo> answerMap =
            competitionProblemRepository.getProblemAnswerInfoByCompetitionId(competitionId)
                .stream()
                .collect(Collectors.toMap(ProblemAnswerInfo::problemId, Function.identity()));

        List<CompetitionGradingInfo> gradingInfos = new ArrayList<>();
        short correctCount = 0;

        for (SubmitInfo submit : req.submittedAnswers()) {
            ProblemAnswerInfo answer = answerMap.get(submit.problemId());
            boolean isCorrect = answer != null && answer.answer() == submit.userChoice();

            if (isCorrect) {
                correctCount++;
            }

            gradingInfos.add(new CompetitionGradingInfo(
                submit.problemId(),
                isCorrect,
                answer != null ? answer.solution() : null
            ));
        }

        return new GradingResult(gradingInfos, correctCount);
    }

    private void persistAndNotify(
        SubmissionContext context, GradingResult grading, int elapsedTime
    ) {
        context.member().plusPoint(PARTICIPATION_POINT);

        competitionResultRepository.save(
            CompetitionResult.of(
                context.competition(),
                context.member(),
                grading.correctCount(),
                elapsedTime
            )
        );

        eventPublisher.publishEvent(
            new PointEvent(validMember, PARTICIPATION_POINT, NotificationType.POINT, false)
        );
    }

    @Retryable(
        retryFor = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    @Override
    public void rewardToWeekOrMonthRanker(SummaryPeriod summaryPeriod, SummaryType summaryType) {
        summaryRepository.fetchTop10Ranker(summaryPeriod, summaryType)
            .forEach(info -> {
                int points = switch (info.ranking().intValue()) {
                    case 1 -> 5;
                    case 2 -> 4;
                    case 3 -> 3;
                    case 4 -> 2;
                    default -> 1;
                };
                info.member().plusPoint(points);
            });
    }

    @Recover
    public void recoverRewardToRanker(
        TransientDataAccessException ex,
        SummaryPeriod summaryPeriod,
        SummaryType summaryType
    ) {
        log.error("[주간, 월간 포인트 지급 실패] 주기={}, 타입={} 포인트 지급 3회 재시도 실패", summaryPeriod, summaryType,
            ex);
        Sentry.captureException(ex);
    }
}
