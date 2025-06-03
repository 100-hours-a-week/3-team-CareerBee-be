package org.choon.careerbee.domain.competition.repository.custom;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;

public interface CompetitionCustomRepository {

    CompetitionProblemResp fetchCompetitionProblemsByCompetitionId(Long competitionId);

    CompetitionIdResp fetchCompetitionIdFromToday(LocalDate today);
}
