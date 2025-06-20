package org.choon.careerbee.domain.company.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class RecruitmentCustomJdbcRepositoryImpl implements
    RecruitmentCustomJdbcRepository {

    private static final String INSERT_SQL = """
        INSERT INTO recruitment
          (company_id, recruiting_id, url, title, start_date, end_date)
        VALUES (?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE id = id
        """;

    private static final int CHUNK = 500;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void batchInsert(List<Recruitment> recruitments) {
        if (recruitments.isEmpty()) return;

        for (int from = 0; from < recruitments.size(); from += CHUNK) {
            int to = Math.min(from + CHUNK, recruitments.size());
            List<Recruitment> sub = recruitments.subList(from, to);

            jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Recruitment recruitment = sub.get(i);
                    ps.setLong  (1, recruitment.getCompany().getId());
                    ps.setLong  (2, recruitment.getRecruitingId());
                    ps.setString(3, recruitment.getUrl());
                    ps.setString(4, recruitment.getTitle());

                    LocalDateTime s = recruitment.getStartDate();
                    LocalDateTime e = recruitment.getEndDate();
                    ps.setTimestamp(5, java.sql.Timestamp.valueOf(s));
                    ps.setTimestamp(6, java.sql.Timestamp.valueOf(e));
                }
                @Override
                public int getBatchSize() { return sub.size(); }
            });
        }
    }
}
