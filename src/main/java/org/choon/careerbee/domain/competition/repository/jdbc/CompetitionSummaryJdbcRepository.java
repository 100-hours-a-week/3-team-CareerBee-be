package org.choon.careerbee.domain.competition.repository.jdbc;

import java.time.LocalDate;
import java.util.List;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;

public interface CompetitionSummaryJdbcRepository {

    /**
     * 지정 기간 summaries 를 멀티-행 INSERT 로 다시 기록한다.
     *
     * @param type        DAY / WEEK / MONTH
     * @param periodStart 기간 시작일 (포함)
     * @param periodEnd   기간 종료일 (포함)
     * @param list        저장할 Summary 엔티티들
     */
    void rewritePeriod(
        SummaryType type,
        LocalDate periodStart, LocalDate periodEnd,
        List<CompetitionSummary> list
    );

    void batchInsert(List<CompetitionSummary> list);
}
