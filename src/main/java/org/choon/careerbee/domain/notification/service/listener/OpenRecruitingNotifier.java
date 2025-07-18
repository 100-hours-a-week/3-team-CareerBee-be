package org.choon.careerbee.domain.notification.service.listener;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.pubsub.RedisPublisher;
import org.choon.careerbee.domain.company.dto.internal.OpenRecruitingEventPayload;
import org.choon.careerbee.domain.notification.dto.event.OpenRecruitingEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRecruitingNotifier {

    private final RedisPublisher redisPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OpenRecruitingEvent event) {
        Map<String, Set<Long>> map = event.notifyMap();
        if (map.isEmpty()) {
            return;
        }

        redisPublisher.publishOpenRecruitingEvent(new OpenRecruitingEventPayload(map));
        log.info("공채 오픈 알림 이벤트 발행 완료 (총 대상자 수: {})",
            map.values().stream().mapToInt(Set::size).sum());
    }
}
