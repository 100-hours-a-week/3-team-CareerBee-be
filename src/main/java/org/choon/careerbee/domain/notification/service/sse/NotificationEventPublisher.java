package org.choon.careerbee.domain.notification.service.sse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.notification.dto.request.PointNotiInfo;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    public void sendPointEarnedNotification(PointNotiInfo pointNotiInfo) {
        notificationRepository.save(
            Notification.of(pointNotiInfo.member(), String.valueOf(pointNotiInfo.point()),
                pointNotiInfo.type(), false)
        );
        sseService.sendTo(pointNotiInfo.member().getId());
    }

    public void sendOpenRecruitingNoti(Map<String, Set<Long>> toNoti) {
        for (Map.Entry<String, Set<Long>> entry : toNoti.entrySet()) {
            String companyName = entry.getKey();
            Set<Long> memberIds = entry.getValue();

            for (Long memberId : memberIds) {
                notificationRepository.save(
                    Notification.of(
                        Member.ofId(memberId), companyName, NotificationType.RECRUITMENT, false
                    )
                );
                sseService.sendTo(memberId);
            }
        }
    }

    public void sendDailyFirstMemberNoti(String firstMemberNickname, List<Long> allMemberIds) {
        log.info("일일 대회 1등 알림 전송 시작");

        List<Notification> notifications = allMemberIds.stream()
            .map(id -> Notification.of(
                Member.ofId(id),
                firstMemberNickname,
                NotificationType.COMPETITION,
                false
            ))
            .toList();
        notificationRepository.saveAll(notifications);

        sseService.sendAll();
    }
}
