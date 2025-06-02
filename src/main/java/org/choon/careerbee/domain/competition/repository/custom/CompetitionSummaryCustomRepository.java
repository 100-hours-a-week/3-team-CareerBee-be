package org.choon.careerbee.domain.competition.repository.custom;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;

public interface CompetitionSummaryCustomRepository {

    CompetitionRankingResp fetchRankings(LocalDate today);

}
