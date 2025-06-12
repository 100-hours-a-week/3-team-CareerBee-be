package org.choon.careerbee.domain.competition.service;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.dto.request.PointNotiInfo;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.service.sse.NotificationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CompetitionCommandServiceImpl implements CompetitionCommandService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionResultRepository competitionResultRepository;
    private final MemberQueryService memberQueryService;
    private final NotificationEventPublisher eventPublisher;

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

        competitionResultRepository.save(
            CompetitionResult.of(validCompetition, validMember, submitReq)
        );

        // 포인트 획득 메시지 전송
        eventPublisher.sendPointEarnedNotification(
            new PointNotiInfo(validMember, 5, NotificationType.POINT, false)
        );

    }
}
