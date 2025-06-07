package org.choon.careerbee.domain.competition.repository.custom.result;

import java.time.LocalDate;
import java.util.List;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;

public interface CompetitionResultCustomRepository {

    List<DailyResultSummaryResp> fetchResultSummaryOfDaily(LocalDate summaryDate);
}
