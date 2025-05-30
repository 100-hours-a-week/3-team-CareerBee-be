package org.choon.careerbee.domain.competition.repository.custom;

import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;

public interface CompetitionCustomRepository {

    CompetitionProblemResp fetchCompetitionProblemsByCompetitionId(Long competitionId);
}
