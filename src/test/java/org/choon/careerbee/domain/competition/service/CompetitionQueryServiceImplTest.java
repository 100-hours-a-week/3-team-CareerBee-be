package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
