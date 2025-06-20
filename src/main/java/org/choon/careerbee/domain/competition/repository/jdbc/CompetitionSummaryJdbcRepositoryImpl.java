package org.choon.careerbee.domain.competition.repository.jdbc;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CompetitionSummaryJdbcRepositoryImpl implements
    CompetitionSummaryJdbcRepository {

    private static final int BATCH_SIZE = 200;
    private static final String DELETE_SQL = """
        DELETE FROM competition_summary
        WHERE type = ?
          AND period_start = ?
          AND period_end   = ?
        """;
    private static final String INSERT_SQL = """
        INSERT INTO competition_summary
          (member_id, solved_count, elapsed_time, ranking,
           max_continuous_day, correct_rate, type,
           period_start, period_end, created_at, modified_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void rewritePeriod(
        SummaryType type,
        LocalDate start, LocalDate end,
        List<CompetitionSummary> list
    ) {

        /* 1) 기존 행 삭제 */
        jdbcTemplate.update(DELETE_SQL, type.name(), start, end);
        if (list.isEmpty()) return;

        /* 2) CHUNK 단위로 배치 인서트 */
        for (int from = 0; from < list.size(); from += BATCH_SIZE) {
            int to = Math.min(from + BATCH_SIZE, list.size());
            List<CompetitionSummary> sub = list.subList(from, to);

            jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i)
                    throws java.sql.SQLException {

                    CompetitionSummary cs = sub.get(i);
                    ps.setLong  (1, cs.getMember().getId());
                    ps.setShort (2, cs.getSolvedCount());
                    ps.setLong  (3, cs.getElapsedTime());
                    ps.setLong  (4, cs.getRanking());
                    ps.setInt   (5, cs.getMaxContinuousDays());
                    ps.setDouble(6, cs.getCorrectRate());
                    ps.setString(7, cs.getType().name());
                    ps.setDate  (8, java.sql.Date.valueOf(cs.getPeriodStart()));
                    ps.setDate  (9, java.sql.Date.valueOf(cs.getPeriodEnd()));
                }
                @Override
                public int getBatchSize() { return sub.size(); }
            });
        }
    }
}
