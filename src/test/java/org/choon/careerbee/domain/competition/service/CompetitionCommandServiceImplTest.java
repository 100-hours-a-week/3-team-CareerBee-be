package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.dto.internal.ProblemAnswerInfo;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq.SubmitInfo;
import org.choon.careerbee.domain.competition.dto.response.CompetitionGradingResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionProblemRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.service.command.CompetitionCommandServiceImpl;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceImplTest {

    @InjectMocks
    private CompetitionCommandServiceImpl competitionCommandService;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Mock
    private CompetitionProblemRepository competitionProblemRepository;

    @Mock
    private CompetitionResultRepository competitionResultRepository;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("대회 참가 - DB 저장 및 캐시 갱신(Write-Through) 성공")
    void joinCompetition_success() {
        // given
        Long competitionId = 1L;
        Long memberId = 10L;
        Competition competition = mock(Competition.class);
        Member member = mock(Member.class);
        RBucket<Boolean> participantBucket = mock(RBucket.class);

        Instant fixedInstant = Instant.parse("2025-07-02T15:00:00Z"); // UTC 기준
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(seoulZone);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(memberQueryService.findById(memberId)).thenReturn(member);
        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId,
            competitionId))
            .thenReturn(false);

        // 캐시 관련 Mock 설정
        when(redissonClient.<Boolean>getBucket(anyString(), any(TypedJsonJacksonCodec.class)))
            .thenReturn(participantBucket);

        // when
        competitionCommandService.joinCompetition(competitionId, memberId);

        // then
        // 1. DB에 저장이 1번 호출되었는지 검증
        verify(competitionParticipantRepository, times(1)).save(any(CompetitionParticipant.class));

        // 2. 캐시가 'true' 값으로 1번 덮어쓰기 되었는지 검증
        verify(participantBucket, times(1)).set(eq(true), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("대회 참가 - 대회가 존재하지 않음")
    void joinCompetition_notFoundCompetition() {
        Long competitionId = 1L;
        Long memberId = 10L;

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionCommandService.joinCompetition(competitionId, memberId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage());

        verify(competitionParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("대회 참가 - 이미 참가한 경우")
    void joinCompetition_alreadyJoined() {
        Long competitionId = 1L;
        Long memberId = 10L;
        Competition competition = mock(Competition.class);
        Member member = mock(Member.class);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(memberQueryService.findById(memberId)).thenReturn(member);
        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId,
            competitionId)).thenReturn(true);

        assertThatThrownBy(() -> competitionCommandService.joinCompetition(competitionId, memberId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.COMPETITION_ALREADY_JOIN.getMessage());

        verify(competitionParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("대회 결과 제출 - 성공")
    void submitCompetitionResult_success() {
        // given
        Long competitionId = 1L;
        Long memberId = 10L;
        Competition competition = mock(Competition.class);
        Member member = mock(Member.class);
        int elapsedTime = 123;

        CompetitionResultSubmitReq submitReq = mock(CompetitionResultSubmitReq.class);
        List<SubmitInfo> submittedAnswers = List.of(
            new SubmitInfo(1L, (short) 5),
            new SubmitInfo(2L, (short) 3),
            new SubmitInfo(3L, (short) 9)  // 틀린 답
        );

        when(submitReq.submittedAnswers()).thenReturn(submittedAnswers);
        when(submitReq.elapsedTime()).thenReturn(elapsedTime);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(memberQueryService.findById(memberId)).thenReturn(member);
        when(competitionResultRepository.existsByMemberIdAndCompetitionId(memberId,
            competitionId)).thenReturn(false);

        when(competitionProblemRepository.getProblemAnswerInfoByCompetitionId(
            competitionId)).thenReturn(
            List.of(
                new ProblemAnswerInfo(1L, (short) 5, "sol1"),
                new ProblemAnswerInfo(2L, (short) 3, "sol2"),
                new ProblemAnswerInfo(3L, (short) 2, "sol3")
            )
        );

        // when
        CompetitionGradingResp resp = competitionCommandService.submitCompetitionResult(
            competitionId, submitReq, memberId);

        // then
        verify(competitionResultRepository, times(1)).save(any(CompetitionResult.class));
        verify(member, times(1)).plusPoint(5);

        assertThat(resp.gradingResults()).hasSize(3);
        assertThat(resp.gradingResults().get(0).isCorrect()).isTrue();
        assertThat(resp.gradingResults().get(1).isCorrect()).isTrue();
        assertThat(resp.gradingResults().get(2).isCorrect()).isFalse();
    }

    @Test
    @DisplayName("대회 결과 제출 - 대회가 존재하지 않음")
    void submitCompetitionResult_competitionNotFound() {
        // given
        Long competitionId = 1L;
        Long memberId = 10L;
        CompetitionResultSubmitReq submitReq = mock(CompetitionResultSubmitReq.class);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            competitionCommandService.submitCompetitionResult(competitionId, submitReq, memberId)
        )
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage());

        verify(competitionResultRepository, never()).save(any());
    }

    @Test
    @DisplayName("대회 결과 제출 - 이미 제출한 경우")
    void submitCompetitionResult_alreadySubmitted() {
        // given
        Long competitionId = 1L;
        Long memberId = 10L;
        Competition competition = mock(Competition.class);
        Member member = mock(Member.class);
        CompetitionResultSubmitReq submitReq = mock(CompetitionResultSubmitReq.class);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(memberQueryService.findById(memberId)).thenReturn(member);
        when(competitionResultRepository.existsByMemberIdAndCompetitionId(memberId, competitionId))
            .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
            competitionCommandService.submitCompetitionResult(competitionId, submitReq, memberId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.RESULT_ALREADY_SUBMIT.getMessage());

        verify(competitionResultRepository, never()).save(any());
    }
}
