package org.choon.careerbee.domain.competition.service.command;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CompetitionCommandServiceImpl implements CompetitionCommandService {

    private static final Integer PARTICIPATION_POINT = 5;

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionResultRepository competitionResultRepository;
    private final CompetitionSummaryRepository summaryRepository;
    private final MemberQueryService memberQueryService;
    private final ApplicationEventPublisher eventPublisher;

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
    public void submitCompetitionResult(
        Long competitionId, CompetitionResultSubmitReq submitReq, Long accessMemberId
    ) {
        Competition validCompetition = competitionRepository.findById(competitionId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST));

        Member validMember = memberQueryService.findById(accessMemberId);

        if (competitionResultRepository
            .existsByMemberIdAndCompetitionId(accessMemberId, competitionId)
        ) {
            throw new CustomException(CustomResponseStatus.RESULT_ALREADY_SUBMIT);
        }

        validMember.plusPoint(PARTICIPATION_POINT);
        competitionResultRepository.save(
            CompetitionResult.of(validCompetition, validMember, submitReq)
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
}
