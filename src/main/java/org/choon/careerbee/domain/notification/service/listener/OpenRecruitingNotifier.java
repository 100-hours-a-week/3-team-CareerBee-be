package org.choon.careerbee.domain.notification.service.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.notification.dto.event.OpenRecruitingEvent;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRecruitingNotifier {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OpenRecruitingEvent event) {
        Map<String, Set<Long>> map = event.notifyMap();
        if (map.isEmpty()) return;

        List<Notification> toInsertNotis = new ArrayList<>();

        map.forEach((companyName, memberIds) ->
            memberIds.forEach(memberId ->
                toInsertNotis.add(Notification.of(
                    Member.ofId(memberId),
                    companyName,
                    NotificationType.RECRUITMENT,
                    false)
                )
            )
        );

        notificationRepository.batchInsert(toInsertNotis);

        map.values().stream()
            .flatMap(java.util.Set::stream)
            .distinct()
            .forEach(sseService::sendTo);

        log.info("공채 오픈 알림 {}건 발송 완료", toInsertNotis.size());
    }
}
