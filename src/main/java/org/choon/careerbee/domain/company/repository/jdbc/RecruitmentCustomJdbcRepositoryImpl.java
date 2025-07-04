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

    private final JdbcTemplate jdbcTemplate;


    @Override
    @Transactional
    public void batchInsert(List<Recruitment> recruitments) {
        if (recruitments.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Recruitment recruitment = recruitments.get(i);
                ps.setLong(1, recruitment.getCompany().getId());
                ps.setLong(2, recruitment.getRecruitingId());
                ps.setString(3, recruitment.getUrl());
                ps.setString(4, recruitment.getTitle());

                LocalDateTime s = recruitment.getStartDate();
                LocalDateTime e = recruitment.getEndDate();
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(s));
                ps.setTimestamp(6, java.sql.Timestamp.valueOf(e));
            }

            @Override
            public int getBatchSize() {
                return recruitments.size();
            }
        });
    }
}
