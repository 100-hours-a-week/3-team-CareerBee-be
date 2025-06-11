package org.choon.careerbee.domain.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.notification.dto.request.ReadNotificationReq;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationQueryService queryService;

    @Override
    public void markAsRead(Long accessMemberId, ReadNotificationReq request) {
        List<Notification> notifications = queryService.fetchNotificationInIds(
            request.notificationIds(), accessMemberId
        );

        if (notifications.size() != request.notificationIds().size()) {
            throw new CustomException(CustomResponseStatus.NOTIFICATION_UPDATE_INVALID);
        }

        notifications.forEach(Notification::markAsRead);
    }
}
