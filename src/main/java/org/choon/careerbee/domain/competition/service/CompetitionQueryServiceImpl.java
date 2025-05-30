package org.choon.careerbee.domain.competition.service;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CompetitionQueryServiceImpl implements CompetitionQueryService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;

    @Override
    public CompetitionParticipationResp checkCompetitionParticipationById(
        Long competitionId, Long accessMemberId
    ) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST);
        }

        return new CompetitionParticipationResp(
            competitionParticipantRepository.existsByMemberIdAndCompetitionId(
                accessMemberId, competitionId)
        );
    }

    @Override
    public CompetitionProblemResp fetchProblems(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST);
        }

        return competitionRepository.fetchCompetitionProblemsByCompetitionId(competitionId);
    }
}
