package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.competition.service.query.CompetitionQueryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


    @Test
    @DisplayName("대회 참여 여부 확인 - 참가한 경우 true 반환")
    void checkCompetitionParticipation_true() {
        // given
        Long competitionId = 1L;
        Long memberId = 100L;

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(competitionParticipantRepository
            .existsByMemberIdAndCompetitionId(memberId, competitionId))
            .thenReturn(true);

        // when
        CompetitionParticipationResp response =
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);

        // then
        assertThat(response.isParticipant()).isTrue();
    }

    @Test
    @DisplayName("대회 참여 여부 확인 - 참가하지 않은 경우 false 반환")
    void checkCompetitionParticipation_false() {
        // given
        Long competitionId = 2L;
        Long memberId = 101L;

        when(competitionRepository.existsById(competitionId)).thenReturn(true);
        when(competitionParticipantRepository.existsByMemberIdAndCompetitionId(memberId,
            competitionId))
            .thenReturn(false);

        // when
        CompetitionParticipationResp response =
            competitionQueryService.checkCompetitionParticipationById(competitionId, memberId);

        // then
        assertThat(response.isParticipant()).isFalse();
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
                1,
                "문제 제목",
                "문제 설명",
                "문제 해설",
                2,
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
        assertThat(problem.number()).isEqualTo(1);
        assertThat(problem.title()).isEqualTo("문제 제목");
        assertThat(problem.description()).isEqualTo("문제 설명");
        assertThat(problem.solution()).isEqualTo("문제 해설");
        assertThat(problem.answer()).isEqualTo(2);

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
            new MemberRankingResp.MemberWeekAndMonthRankInfo(3L, 7, 0.75);

        MemberRankingResp.MemberWeekAndMonthRankInfo monthRank =
            new MemberRankingResp.MemberWeekAndMonthRankInfo(2L, 15, 0.88);

        MemberRankingResp mockResp = new MemberRankingResp(dayRank, weekRank, monthRank);

        when(competitionSummaryRepository.fetchMemberRankingById(anyLong(), any(LocalDate.class))).thenReturn(mockResp);

        // when
        MemberRankingResp result = competitionQueryService.fetchMemberCompetitionRankingById(
            memberId, LocalDate.now()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.daily().rank()).isEqualTo(5L);
        assertThat(result.week().rank()).isEqualTo(3L);
        assertThat(result.month().rank()).isEqualTo(2L);

        verify(competitionSummaryRepository, times(1)).fetchMemberRankingById(memberId, LocalDate.now());
    }
}
