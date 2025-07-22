package org.choon.careerbee.domain.notification.service.processor;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.pubsub.dto.DailyWinnerEventPayload;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyWinnerNotificationProcessor {

    private final MemberQueryService memberQueryService;
    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    public void process(DailyWinnerEventPayload event) {
        List<Long> ids = memberQueryService.findAllMemberIds();
        List<Notification> batch = ids.stream()
            .map(id -> Notification.of(
                Member.ofId(id),
                event.winnerNickname(),
                NotificationType.COMPETITION,
                false))
            .toList();

        notificationRepository.batchInsert(batch);
        ids.forEach(sseService::sendTo);

        log.info("일일 1등 알림 {}건 발송 완료", ids.size());
    }
}
