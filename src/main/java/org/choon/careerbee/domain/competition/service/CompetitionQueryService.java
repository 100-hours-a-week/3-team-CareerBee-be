package org.choon.careerbee.domain.competition.service;

import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;

public interface CompetitionQueryService {

    CompetitionParticipationResp checkCompetitionParticipationById(
        Long competitionId, Long accessMemberId
    );

    CompetitionProblemResp fetchProblems(Long competitionId);
}
