package org.choon.careerbee.domain.notification.service.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.notification.dto.request.PointNotiInfo;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    public void sendPointEarnedNotification(PointNotiInfo pointNotiInfo) {
        String content = "CS 대회 참가로 포인트 " + pointNotiInfo.point() + "점을 획득했어요.";
        notificationRepository.save(
            Notification.of(pointNotiInfo.member(), content, pointNotiInfo.type(), false)
        );
        sseService.sendTo(pointNotiInfo.member().getId(), content);
    }
}
