package org.choon.careerbee.domain.notification.service.sse;

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
        String content = "CS 대회 참가로 포인트 " + pointNotiInfo.point() + "점을 획득했어요.";
        notificationRepository.save(
            Notification.of(pointNotiInfo.member(), content, pointNotiInfo.type(), false)
        );
        sseService.sendTo(pointNotiInfo.member().getId(), content);
    }

    public void sendOpenRecruitingNoti(Map<String, Set<Long>> toNoti) {
        for (Map.Entry<String, Set<Long>> entry : toNoti.entrySet()) {
            String companyName = entry.getKey();
            Set<Long> memberIds = entry.getValue();

            String message = "저장하신 " + companyName + "사의 채용 공고를 확인해보세요!";

            for (Long memberId : memberIds) {

                notificationRepository.save(
                    Notification.of(
                        Member.ofId(memberId), message, NotificationType.RECRUITMENT, false
                    )
                );
                sseService.sendTo(memberId, message);
            }
        }
    }
}
