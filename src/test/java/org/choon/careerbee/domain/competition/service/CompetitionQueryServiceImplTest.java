package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
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
        when(competitionRepository.fetchCompetitionProblemsByCompetitionId(competitionId)).thenReturn(mockResp);

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
            .extracting(CompetitionProblemResp.ProblemChoiceInfo::order, CompetitionProblemResp.ProblemChoiceInfo::content)
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
}
