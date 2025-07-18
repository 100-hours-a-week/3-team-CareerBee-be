package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp.RankerInfo;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.competition.service.query.CompetitionQueryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class CompetitionQueryServiceImplTest {

    @InjectMocks
    private CompetitionQueryServiceImpl competitionQueryService;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Mock
    private CompetitionSummaryRepository competitionSummaryRepository;

    @Mock
    private CompetitionResultRepository competitionResultRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private Clock clock;

//    @Test
//    @DisplayName("대회 참여 여부 확인 - 캐시가 존재할 경우(HIT), DB 조회 없이 캐시에서 즉시 반환")
//    void checkCompetitionParticipation_cacheHit() {
//        // given
//        Long competitionId = 1L;
//        Long memberId = 100L;
//        RBucket<Boolean> participantBucket = mock(RBucket.class);
//        String expectedKey = "member:" + memberId + ":participant:20250702";
//        Instant fixedInstant = Instant.parse("2025-07-02T10:00:00Z");
//        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
//
//        when(clock.instant()).thenReturn(fixedInstant);
//        when(clock.getZone()).thenReturn(seoulZone);
//
//        when(competitionRepository.existsById(competitionId)).thenReturn(true);
//        when(redissonClient.<Boolean>getBucket(anyString(),
//            any(TypedJsonJacksonCodec.class))).thenReturn(participantBucket);
//        when(participantBucket.isExists()).thenReturn(true);
//        when(participantBucket.get()).thenReturn(true);
//
//        // when
//        CompetitionParticipationResp response =
//            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);
//
//        // then
//        assertThat(response.isParticipant()).isTrue();
//        verify(competitionParticipantRepository, never()).existsByMemberIdAndCompetitionId(
//            anyLong(), anyLong());
//        verify(participantBucket, never()).set(anyBoolean(), anyLong(), any(TimeUnit.class));
//    }

//    @Test
//    @DisplayName("대회 참여 여부 확인 - 캐시가 없을 경우(MISS), DB 조회 후 캐시에 저장하고 결과를 반환")
//    void checkCompetitionParticipation_cacheMiss() {
//        // given
//        Long competitionId = 2L;
//        Long memberId = 101L;
//        Instant fixedInstant = Instant.parse("2025-07-02T14:00:00Z");
//        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
//        RBucket<Boolean> participantBucket = mock(RBucket.class);
//
//        when(clock.instant()).thenReturn(fixedInstant);
//        when(clock.getZone()).thenReturn(seoulZone);
//        when(competitionRepository.existsById(competitionId)).thenReturn(true);
//        when(redissonClient.<Boolean>getBucket(anyString(), any(TypedJsonJacksonCodec.class)))
//            .thenReturn(participantBucket);
//        when(participantBucket.isExists()).thenReturn(false);
//        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId,
//            competitionId))
//            .thenReturn(true);
//
//        // when
//        CompetitionParticipationResp response =
//            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);
//
//        // then
//        assertThat(response.isParticipant()).isTrue();
//
//        verify(competitionParticipantRepository, times(1))
//            .existsByMemberIdAndCompetitionId(memberId, competitionId);
//        verify(participantBucket, times(1))
//            .set(eq(true), anyLong(), any(TimeUnit.class));
//    }

    @Test
    @DisplayName("대회 참여 여부 확인 - 존재하지 않는 대회일 경우 예외 발생")
    void checkCompetitionParticipation_notFoundCompetition_throwsException() {
        // given
        Long competitionId = 999L;
        Long memberId = 123L;

        when(competitionRepository.existsById(competitionId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId)
        )
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage());

        verify(competitionParticipantRepository, never()).existsByMemberIdAndCompetitionId(any(),
            any());
    }

    @Test
    @DisplayName("대회 문제 조회 - 반환된 DTO 내용 비교")
    void fetchProblems_success_withContentValidation() {
        // given
        Long competitionId = 100L;

        List<CompetitionProblemResp.ProblemChoiceInfo> choices = List.of(
            new CompetitionProblemResp.ProblemChoiceInfo(1, "보기 A"),
            new CompetitionProblemResp.ProblemChoiceInfo(2, "보기 B")
        );

        List<CompetitionProblemResp.ProblemInfo> problemInfos = List.of(
            new CompetitionProblemResp.ProblemInfo(
                1L,
                1,
                "문제 제목",
                "문제 설명",
                choices
            )
        );

        CompetitionProblemResp mockResp = new CompetitionProblemResp(problemInfos);

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(competitionRepository.fetchCompetitionProblemsByCompetitionId(
            competitionId)).thenReturn(mockResp);

        // when
        CompetitionProblemResp result = competitionQueryService.fetchProblems(competitionId);

        // then
        assertThat(result.problems()).hasSize(1);

        CompetitionProblemResp.ProblemInfo problem = result.problems().get(0);
        assertThat(problem.id()).isEqualTo(1L);
        assertThat(problem.number()).isEqualTo(1);
        assertThat(problem.title()).isEqualTo("문제 제목");
        assertThat(problem.description()).isEqualTo("문제 설명");

        assertThat(problem.choices()).hasSize(2);
        assertThat(problem.choices())
            .extracting(CompetitionProblemResp.ProblemChoiceInfo::order,
                CompetitionProblemResp.ProblemChoiceInfo::content)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(1, "보기 A"),
                org.assertj.core.groups.Tuple.tuple(2, "보기 B")
            );

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(competitionRepository).fetchCompetitionProblemsByCompetitionId(captor.capture());
        assertThat(captor.getValue()).isEqualTo(competitionId);
    }

    @Test
    @DisplayName("대회 문제 조회 - 존재하지 않는 대회일 경우 예외 발생")
    void fetchProblems_competitionNotFound_throwsException() {
        // given
        Long nonExistCompetitionId = 999L;
        when(competitionRepository.existsById(nonExistCompetitionId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
            competitionQueryService.fetchProblems(nonExistCompetitionId)
        )
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage());

        verify(competitionRepository, never()).fetchCompetitionProblemsByCompetitionId(any());
    }

    @Test
    @DisplayName("대회 랭킹 조회 - 정상적으로 반환되는 경우")
    void fetchRankings_success() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        CompetitionRankingResp mockResp = new CompetitionRankingResp(
            List.of(),
            List.of(),
            List.of()
        );

        when(competitionSummaryRepository.fetchRankings(today)).thenReturn(mockResp);

        // when
        CompetitionRankingResp result = competitionQueryService.fetchRankings(today);

        // then
        assertThat(result).isEqualTo(mockResp);
        verify(competitionSummaryRepository).fetchRankings(today);

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(competitionSummaryRepository, times(1)).fetchRankings(captor.capture());
        assertThat(captor.getValue()).isEqualTo(today);
    }

    @Test
    @DisplayName("오늘 날짜 기준으로 대회 ID 조회 - 정상 반환")
    void fetchCompetitionIdBy_success() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        CompetitionIdResp mockResp = new CompetitionIdResp(42L);

        when(competitionRepository.fetchCompetitionIdFromToday(today)).thenReturn(mockResp);

        // when
        CompetitionIdResp result = competitionQueryService.fetchCompetitionIdBy(today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.competitionId()).isEqualTo(42L);

        verify(competitionRepository, times(1)).fetchCompetitionIdFromToday(today);
    }

    @Test
    @DisplayName("오늘 날짜 기준으로 대회 ID 조회 - 해당 날짜 대회 없으면 null 반환")
    void fetchCompetitionIdBy_noneExist_returnsNull() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);

        when(competitionRepository.fetchCompetitionIdFromToday(today)).thenReturn(null);

        // when
        CompetitionIdResp result = competitionQueryService.fetchCompetitionIdBy(today);

        // then
        assertThat(result).isNull();

        verify(competitionRepository).fetchCompetitionIdFromToday(today);
    }

    @Test
    @DisplayName("내 랭킹 조회 - 정상적으로 반환되는 경우")
    void fetchMemberCompetitionRankingById_success() {
        // given
        Long memberId = 123L;
        MemberRankingResp.MemberDayRankInfo dayRank =
            new MemberRankingResp.MemberDayRankInfo(5L, 1234L, (short) 3);

        MemberRankingResp.MemberWeekAndMonthRankInfo weekRank =
            MemberRankingResp.MemberWeekAndMonthRankInfo.from(3L, 7, 0.75);

        MemberRankingResp.MemberWeekAndMonthRankInfo monthRank =
            MemberRankingResp.MemberWeekAndMonthRankInfo.from(2L, 15, 0.88);

        MemberRankingResp mockResp = new MemberRankingResp(dayRank, weekRank, monthRank);

        when(competitionSummaryRepository.fetchMemberRankingById(anyLong(),
            any(LocalDate.class))).thenReturn(mockResp);

        // when
        MemberRankingResp result = competitionQueryService.fetchMemberCompetitionRankingById(
            memberId, LocalDate.now()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.daily().rank()).isEqualTo(5L);
        assertThat(result.week().rank()).isEqualTo(3L);
        assertThat(result.month().rank()).isEqualTo(2L);

        verify(competitionSummaryRepository, times(1)).fetchMemberRankingById(memberId,
            LocalDate.now());
    }

    @Test
    @DisplayName("실시간 내 랭킹 조회 - 정상 응답 반환")
    void fetchMemberLiveRanking_success() {
        // given
        Long memberId = 123L;
        LocalDate today = LocalDate.of(2025, 6, 10);

        MemberLiveRankingResp mockResp = new MemberLiveRankingResp(
            7L,
            123234,
            (short) 3
        );

        when(competitionResultRepository.fetchMemberLiveRankingByDate(memberId, today))
            .thenReturn(Optional.of(mockResp));

        // when
        MemberLiveRankingResp result = competitionQueryService.fetchMemberLiveRanking(
            memberId, today
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.rank()).isEqualTo(7L);
        assertThat(result.solvedCount()).isEqualTo((short) 3);
        assertThat(result.elapsedTime()).isEqualTo(123234);

        verify(competitionResultRepository, times(1)).fetchMemberLiveRankingByDate(memberId, today);
    }

    @Test
    @DisplayName("실시간 내 랭킹 조회 - 데이터 없을 경우 null 반환")
    void fetchMemberLiveRanking_notFound_throwsException() {
        // given
        Long memberId = 456L;
        LocalDate today = LocalDate.of(2025, 6, 10);

        when(competitionResultRepository.fetchMemberLiveRankingByDate(memberId, today))
            .thenReturn(Optional.empty());

        // when & then
        MemberLiveRankingResp actualResp = competitionQueryService.fetchMemberLiveRanking(memberId,
            today);
        assertThat(actualResp).isNull();

        verify(competitionResultRepository).fetchMemberLiveRankingByDate(memberId, today);
    }

    @Test
    @DisplayName("실시간 랭킹 조회 - 정상적으로 실시간 랭킹 조회")
    void fetchLiveRanking_success() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 10);
        LiveRankingResp mockResp = new LiveRankingResp(
            List.of(
                new RankerInfo(1L, "user1", "url1", 123000, (short) 5),
                new RankerInfo(2L, "user2", "url2", 123001, (short) 4),
                new RankerInfo(3L, "user3", "url3", 123002, (short) 3),
                new RankerInfo(4L, "user4", "url4", 123003, (short) 3),
                new RankerInfo(5L, "user5", "url5", 123004, (short) 2)
            )
        );
        when(competitionResultRepository.fetchLiveRankingByDate(today)).thenReturn(mockResp);

        // when
        LiveRankingResp result = competitionQueryService.fetchLiveRanking(today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.rankings().size()).isEqualTo(5L);
        assertThat(result.rankings().getFirst().rank()).isEqualTo(1L);
        assertThat(result.rankings().getFirst().nickname()).isEqualTo("user1");
        assertThat(result.rankings().getFirst().elapsedTime()).isEqualTo(123000);
        assertThat(result.rankings().getFirst().solvedCount()).isEqualTo((short) 5);
        verify(competitionResultRepository).fetchLiveRankingByDate(today);
    }

}
