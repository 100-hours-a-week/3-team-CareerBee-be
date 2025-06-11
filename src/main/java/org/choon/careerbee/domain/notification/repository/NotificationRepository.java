package org.choon.careerbee.domain.notification.repository;

import org.choon.careerbee.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
