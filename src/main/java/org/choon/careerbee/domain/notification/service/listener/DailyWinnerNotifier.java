package org.choon.careerbee.domain.notification.service.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.pubsub.RedisPublisher;
import org.choon.careerbee.common.pubsub.dto.DailyWinnerEventPayload;
import org.choon.careerbee.domain.competition.dto.event.DailyWinnerCalculated;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyWinnerNotifier {

    private final RedisPublisher redisPublisher;

    /**
     * 집계 트랜잭션이 **정상 커밋된 후**에만 호출된다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DailyWinnerCalculated e) {
        log.info("일일 1등 알림 전송 시작: {}, {}", e.winnerNickname(), e.day());
        redisPublisher.publishDailyWinnerEvent(new DailyWinnerEventPayload(e.winnerNickname()));
    }
}
