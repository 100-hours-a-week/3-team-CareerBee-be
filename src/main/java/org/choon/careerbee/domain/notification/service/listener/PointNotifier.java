package org.choon.careerbee.domain.notification.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.pubsub.RedisPublisher;
import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.choon.careerbee.domain.notification.service.NotificationCommandService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointNotifier {

    private final RedisPublisher redisPublisher;
    private final NotificationCommandService commandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PointEvent pointEvent) {
        log.info("포인트 획득 알림 전송 시작");

        commandService.saveNoti(pointEvent);
        redisPublisher.publishPointEvent(pointEvent);
    }
}
