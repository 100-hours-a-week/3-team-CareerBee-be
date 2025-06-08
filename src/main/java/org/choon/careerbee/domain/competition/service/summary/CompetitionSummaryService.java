package org.choon.careerbee.domain.competition.service.summary;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;

public interface CompetitionSummaryService {

    void dailySummary(LocalDate now);

    void weekAndMonthSummary(SummaryPeriod summaryPeriod, SummaryType summaryType);
}
