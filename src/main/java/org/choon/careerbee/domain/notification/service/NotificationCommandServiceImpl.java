package org.choon.careerbee.domain.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.choon.careerbee.domain.notification.dto.request.ReadNotificationReq;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationQueryService queryService;
    private final NotificationRepository notificationRepository;

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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveNoti(PointEvent pointEvent) {
        notificationRepository.save(
            Notification.of(
                pointEvent.member(),
                String.valueOf(pointEvent.point()),
                pointEvent.type(),
                false
            )
        );
    }
}
