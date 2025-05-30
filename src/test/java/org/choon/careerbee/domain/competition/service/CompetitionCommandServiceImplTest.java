package org.choon.careerbee.domain.competition.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceImplTest {

    @InjectMocks
    private CompetitionCommandServiceImpl competitionCommandService;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Mock
    private MemberQueryService memberQueryService;

    @Test
    @DisplayName("대회 참가 - 성공")
    void joinCompetition_success() {
        Long competitionId = 1L;
        Long memberId = 10L;
        Competition competition = mock(Competition.class);
        Member member = mock(Member.class);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(memberQueryService.findById(memberId)).thenReturn(member);
        when(competitionParticipantRepository
            .existsByMemberIdAndCompetitionId(memberId, competitionId))
            .thenReturn(false);

        competitionCommandService.joinCompetition(competitionId, memberId);

        verify(competitionParticipantRepository, times(1)).save(any(CompetitionParticipant.class));
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
}
