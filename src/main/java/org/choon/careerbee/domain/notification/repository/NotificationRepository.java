package org.choon.careerbee.domain.notification.repository;

import java.util.List;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.repository.custom.NotificationCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends
    JpaRepository<Notification, Long>, NotificationCustomRepository {

    List<Notification> findByIdInAndMemberId(List<Long> ids, Long memberId);

}
