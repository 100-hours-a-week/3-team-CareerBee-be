package org.choon.careerbee.util.date;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;

public class DateUtil {

    public static SummaryPeriod getPeriod(LocalDate now, SummaryType summaryType) {
        if (summaryType.equals(SummaryType.WEEK)) {
            LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            return new SummaryPeriod(startOfWeek, endOfWeek);
        }

        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        return new SummaryPeriod(startOfMonth, endOfMonth);

    }
}
