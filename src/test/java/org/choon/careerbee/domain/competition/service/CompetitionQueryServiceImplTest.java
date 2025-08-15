package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
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
import org.redisson.api.RBucket;
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
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("대회 참여 여부 - 캐시 히트 시 캐시에서 바로 반환하고 DB 조회/캐시 저장 없음")
    void checkCompetitionParticipation_cacheHit_returnsFromCache() throws Exception {
        // given
        Long competitionId = 100L;
        Long memberId = 200L;

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);
        String json = "true";

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(json);
        when(objectMapper.readValue(json, Boolean.class)).thenReturn(true);

        // when
        CompetitionParticipationResp resp =
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.isParticipant()).isTrue();

        // 캐시 히트이므로 DB 조회/캐시 set 없음
        verify(competitionParticipantRepository, never())
            .existsByMemberIdAndCompetitionId(anyLong(), anyLong());
        verify(bucket, never()).set(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("대회 참여 여부 - 캐시 미스 시 DB 조회 후 캐시에 저장하고 반환 (참여자=true)")
    void checkCompetitionParticipation_cacheMiss_hitsDb_thenCachesTrue() throws Exception {
        // given
        Long competitionId = 100L;
        Long memberId = 200L;

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);
        String json = "true";

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null); // 캐시 미스
        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId, competitionId))
            .thenReturn(true);
        when(objectMapper.writeValueAsString(true)).thenReturn(json);

        // when
        CompetitionParticipationResp resp =
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.isParticipant()).isTrue();

        verify(competitionParticipantRepository, times(1))
            .existsByMemberIdAndCompetitionId(memberId, competitionId);
        verify(bucket, times(1)).set(eq(json), any(Duration.class));
    }

    @Test
    @DisplayName("대회 참여 여부 - 캐시 미스 시 DB 조회 후 캐시에 저장하고 반환 (비참여=false)")
    void checkCompetitionParticipation_cacheMiss_hitsDb_thenCachesFalse() throws Exception {
        // given
        Long competitionId = 101L;
        Long memberId = 201L;

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);
        String json = "false";

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null); // 캐시 미스
        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId, competitionId))
            .thenReturn(false);
        when(objectMapper.writeValueAsString(false)).thenReturn(json);

        // when
        CompetitionParticipationResp resp =
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.isParticipant()).isFalse();

        verify(competitionParticipantRepository, times(1))
            .existsByMemberIdAndCompetitionId(memberId, competitionId);
        verify(bucket, times(1)).set(eq(json), any(Duration.class));
    }

    @Test
    @DisplayName("대회 참여 여부 - 캐시 히트 JSON 파싱 실패 시 CustomException(JSON_PARSING_ERROR)")
    void checkCompetitionParticipation_cacheHit_jsonReadError_throwsCustom() throws Exception {
        // given
        Long competitionId = 100L;
        Long memberId = 200L;

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn("malformed");
        when(objectMapper.readValue("malformed", Boolean.class))
            .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("bad") {});

        // when & then
        assertThatThrownBy(() ->
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId)
        )
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.JSON_PARSING_ERROR.getMessage());

        // 파싱 단계에서 터졌으므로 DB 조회/캐시 set 없음
        verify(competitionParticipantRepository, never())
            .existsByMemberIdAndCompetitionId(anyLong(), anyLong());
        verify(bucket, never()).set(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("대회 참여 여부 - 캐시 미스 후 JSON 직렬화 실패 시 CustomException(JSON_PARSING_ERROR)")
    void checkCompetitionParticipation_cacheMiss_jsonWriteError_throwsCustom() throws Exception {
        // given
        Long competitionId = 100L;
        Long memberId = 200L;

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null); // 캐시 미스
        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId, competitionId))
            .thenReturn(true);
        when(objectMapper.writeValueAsString(true))
            .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("bad") {});

        // when & then
        assertThatThrownBy(() ->
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId)
        )
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.JSON_PARSING_ERROR.getMessage());

        // 직렬화 단계에서 터졌으므로 버킷 set 호출 안 됨
        verify(bucket, never()).set(anyString(), any(Duration.class));
    }


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
    @DisplayName("대회 랭킹 조회 - 캐시 미스 시 DB 조회 후 캐시에 저장하고 반환")
    void fetchRankings_cacheMiss() throws Exception {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        CompetitionRankingResp mockResp = new CompetitionRankingResp(List.of(), List.of(), List.of());
        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);

        String json = "{\"ok\":true}";

        // Redis 버킷 및 캐시 미스 스텁
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        // DB 조회 스텁
        when(competitionSummaryRepository.fetchRankings(today)).thenReturn(mockResp);

        // 직렬화 스텁 (캐시에 저장할 때 사용)
        when(objectMapper.writeValueAsString(mockResp)).thenReturn(json);

        // when
        CompetitionRankingResp result = competitionQueryService.fetchRankings(today);

        // then
        assertThat(result).isEqualTo(mockResp);
        verify(competitionSummaryRepository, times(1)).fetchRankings(today);
        verify(bucket, times(1)).set(eq(json), any(Duration.class));
    }

    @Test
    @DisplayName("대회 랭킹 조회 - 캐시 히트 시 DB 조회 없이 캐시에서 반환")
    void fetchRankings_cacheHit() throws Exception {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        CompetitionRankingResp expected = new CompetitionRankingResp(List.of(), List.of(), List.of());
        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);

        String json = "{\"ok\":true}";

        // Redis 버킷 및 캐시 히트 스텁
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(json);

        // 역직렬화 스텁
        when(objectMapper.readValue(json, CompetitionRankingResp.class)).thenReturn(expected);

        // when
        CompetitionRankingResp result = competitionQueryService.fetchRankings(today);

        // then
        assertThat(result).isEqualTo(expected);
        verify(competitionSummaryRepository, never()).fetchRankings(any());
        verify(bucket, never()).set(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 캐시 미스 시 DB 조회 후 캐시에 저장하고 반환")
    void fetchCompetitionIdBy_cacheMiss_returnsAndCaches() throws Exception {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        CompetitionIdResp mockResp = new CompetitionIdResp(42L);

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);
        String json = "{\"id\":42}";

        // Redis 버킷/캐시 미스
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        // DB 조회
        when(competitionRepository.fetchCompetitionIdFromToday(today)).thenReturn(mockResp);

        // 직렬화하여 캐시 저장
        when(objectMapper.writeValueAsString(mockResp)).thenReturn(json);

        // when
        CompetitionIdResp result = competitionQueryService.fetchCompetitionIdBy(today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.competitionId()).isEqualTo(42L);

        verify(competitionRepository, times(1)).fetchCompetitionIdFromToday(today);
        verify(bucket, times(1)).set(eq(json), any(Duration.class));
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 캐시 히트 시 DB 조회 없이 캐시에서 반환")
    void fetchCompetitionIdBy_cacheHit_returnsFromCache() throws Exception {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        CompetitionIdResp expected = new CompetitionIdResp(42L);

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);
        String json = "{\"id\":42}";

        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(json);
        when(objectMapper.readValue(json, CompetitionIdResp.class)).thenReturn(expected);

        // when
        CompetitionIdResp result = competitionQueryService.fetchCompetitionIdBy(today);

        // then
        assertThat(result).isEqualTo(expected);
        verify(competitionRepository, never()).fetchCompetitionIdFromToday(any());
        verify(bucket, never()).set(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 캐시 미스이고 해당 날짜 대회 없으면 null 반환(캐시 저장 안 함)")
    void fetchCompetitionIdBy_cacheMiss_noneExist_returnsNull() throws Exception {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);

        @SuppressWarnings("unchecked")
        RBucket<String> bucket = (RBucket<String>) mock(RBucket.class);

        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null); // 캐시 미스
        when(competitionRepository.fetchCompetitionIdFromToday(today)).thenReturn(null); // DB에도 없음

        // when
        CompetitionIdResp result = competitionQueryService.fetchCompetitionIdBy(today);

        // then
        assertThat(result).isNull();
        verify(competitionRepository, times(1)).fetchCompetitionIdFromToday(today);
        verify(bucket, never()).set(anyString(), any(Duration.class)); // 캐시에 저장하지 않음
    }

    @Test
    @DisplayName("내 랭킹 조회 - 캐시 미스 시 DB 조회 후 캐시에 저장하고 반환")
    void fetchMemberCompetitionRankingById_success_cacheMiss() throws Exception {
        // given
        Long memberId = 123L;
        LocalDate today = LocalDate.of(2025, 6, 2);

        MemberRankingResp.MemberDayRankInfo dayRank = new MemberRankingResp.MemberDayRankInfo(5L, 1234L, (short) 3);
        MemberRankingResp.MemberWeekAndMonthRankInfo weekRank = MemberRankingResp.MemberWeekAndMonthRankInfo.from(3L, 7, 0.75);
        MemberRankingResp.MemberWeekAndMonthRankInfo monthRank = MemberRankingResp.MemberWeekAndMonthRankInfo.from(2L, 15, 0.88);
        MemberRankingResp mockResp = new MemberRankingResp(dayRank, weekRank, monthRank);

        RBucket<String> bucket = mock(RBucket.class);
        String json = "{\"dummy\":\"ok\"}"; // 아무 문자열이면 됨

        // 레디스 버킷 호출 스텁
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null); // 캐시 미스
        // DB 조회 스텁
        when(competitionSummaryRepository.fetchMemberRankingById(memberId, today))
            .thenReturn(mockResp);
        // 캐시 저장 시 직렬화 스텁
        when(objectMapper.writeValueAsString(mockResp)).thenReturn(json);

        // when
        MemberRankingResp result =
            competitionQueryService.fetchMemberCompetitionRankingById(memberId, today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.daily().rank()).isEqualTo(5L);
        assertThat(result.week().rank()).isEqualTo(3L);
        assertThat(result.month().rank()).isEqualTo(2L);

        verify(competitionSummaryRepository, times(1))
            .fetchMemberRankingById(memberId, today);
        verify(bucket, times(1))
            .set(eq(json), any(Duration.class));
    }

    @Test
    @DisplayName("내 랭킹 조회 - 캐시 히트 시 DB 조회 없이 반환")
    void fetchMemberCompetitionRankingById_cacheHit() throws Exception {
        Long memberId = 123L;
        LocalDate today = LocalDate.of(2025, 6, 2);

        MemberRankingResp.MemberDayRankInfo dayRank = new MemberRankingResp.MemberDayRankInfo(5L, 1234L, (short) 3);
        MemberRankingResp.MemberWeekAndMonthRankInfo weekRank = MemberRankingResp.MemberWeekAndMonthRankInfo.from(3L, 7, 0.75);
        MemberRankingResp.MemberWeekAndMonthRankInfo monthRank = MemberRankingResp.MemberWeekAndMonthRankInfo.from(2L, 15, 0.88);
        MemberRankingResp expected = new MemberRankingResp(dayRank, weekRank, monthRank);
        String json = "{\"dummy\":\"ok\"}";

        RBucket<String> bucket = mock(RBucket.class);

        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(json);
        when(objectMapper.readValue(json, MemberRankingResp.class)).thenReturn(expected);

        MemberRankingResp result =
            competitionQueryService.fetchMemberCompetitionRankingById(memberId, today);

        assertThat(result).isEqualTo(expected);
        verify(competitionSummaryRepository, never()).fetchMemberRankingById(anyLong(), any());
        verify(bucket, never()).set(anyString(), any(Duration.class));
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
            .thenReturn(Optional.of(mockResp));// when
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
