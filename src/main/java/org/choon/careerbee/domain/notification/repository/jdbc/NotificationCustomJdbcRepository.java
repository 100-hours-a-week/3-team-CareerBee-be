package org.choon.careerbee.domain.notification.repository.jdbc;

import java.util.List;
import org.choon.careerbee.domain.notification.entity.Notification;

public interface NotificationCustomJdbcRepository {

    void batchInsert(List<Notification> notifications);
}
