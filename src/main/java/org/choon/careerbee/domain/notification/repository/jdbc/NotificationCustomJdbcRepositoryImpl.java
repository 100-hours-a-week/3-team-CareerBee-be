package org.choon.careerbee.domain.notification.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class NotificationCustomJdbcRepositoryImpl implements
    NotificationCustomJdbcRepository {
    private static final int CHUNK = 200;
    private static final String INSERT_SQL = """
        INSERT INTO notification
          (member_id, content, type, is_read, created_at, modified_at)
        VALUES (?, ?, ?, ?, NOW(), NOW())
        ON DUPLICATE KEY UPDATE id = id
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void batchInsert(List<Notification> list) {
        if (list.isEmpty()) return;

        for (int from = 0; from < list.size(); from += CHUNK) {
            int to = Math.min(from + CHUNK, list.size());
            List<Notification> sub = list.subList(from, to);

            jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Notification n = sub.get(i);
                    ps.setLong  (1, n.getMember().getId());
                    ps.setString(2, n.getContent());
                    ps.setString(3, n.getType().name());
                    ps.setBoolean(4, n.getIsRead());
                }
                @Override
                public int getBatchSize() { return sub.size(); }
            });
        }
    }
}
