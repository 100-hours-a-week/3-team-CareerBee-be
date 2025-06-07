package org.choon.careerbee.domain.competition.service.summary;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;

public interface CompetitionSummaryService {

    void dailySummary(LocalDate now);

    void weekAndMonthSummary(LocalDate now, SummaryType summaryType);
}
