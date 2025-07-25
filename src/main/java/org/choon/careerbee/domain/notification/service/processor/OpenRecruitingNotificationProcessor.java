package org.choon.careerbee.domain.notification.service.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.pubsub.dto.OpenRecruitingEventPayload;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRecruitingNotificationProcessor {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    public void process(OpenRecruitingEventPayload event) {
        Map<String, Set<Long>> map = event.notifyMap();
        if (map.isEmpty()) {
            log.info("공채 오픈 알림 대상자가 없어 알림 전송을 건너뜁니다.");
            return;
        }

        List<Notification> notifications = new ArrayList<>();

        map.forEach((companyName, memberIds) ->
            memberIds.forEach(memberId ->
                notifications.add(Notification.of(
                    Member.ofId(memberId),
                    companyName,
                    NotificationType.RECRUITMENT,
                    false))
            )
        );

        notificationRepository.batchInsert(notifications);

        map.values().stream()
            .flatMap(Set::stream)
            .distinct()
            .forEach(sseService::sendTo);

        log.info("공채 오픈 알림 {}건 DB 저장 및 SSE 발송 완료", notifications.size());
    }
}
