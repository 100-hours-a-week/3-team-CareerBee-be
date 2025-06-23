package org.choon.careerbee.domain.notification.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointNotifier {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PointEvent pointEvent) {
        log.info("포인트 획득 알림 전송 시작");

        notificationRepository.save(
            Notification.of(
                pointEvent.member(),
                String.valueOf(pointEvent.point()),
                pointEvent.type(),
                false
            )
        );

        sseService.sendTo(pointEvent.member().getId());
    }
}
