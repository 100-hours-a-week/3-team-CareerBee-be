package org.choon.careerbee.domain.company.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.request.RecentIssueUpdateReq;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompanyJdbcRepositoryImpl implements CompanyJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL = """
        UPDATE company
        SET recent_issue = ?
        WHERE name = ?
        """;

    @Override
    public void batchUpdateRecentIssues(List<RecentIssueUpdateReq> updateRequests) {
        jdbcTemplate.batchUpdate(SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RecentIssueUpdateReq req = updateRequests.get(i);
                ps.setString(1, req.newRecentIssue());
                ps.setString(2, req.companyName());
            }

            @Override
            public int getBatchSize() {
                return updateRequests.size();
            }
        });
    }
}
