package org.choon.careerbee.domain.notification.service.listener;


import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.dto.event.DailyWinnerCalculated;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
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
public class DailyWinnerNotifier {

    private final NotificationRepository notificationRepository;
    private final MemberQueryService memberQuery;
    private final SseService sse;

    /**
     * 집계 트랜잭션이 **정상 커밋된 후**에만 호출된다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DailyWinnerCalculated e) {

        log.info("일일 1등 알림 전송 시작: {}, {}", e.winnerNickname(), e.day());

        // 1) 알림 엔티티 생성
        List<Long> ids = memberQuery.findAllMemberIds();
        List<Notification> batch = ids.stream()
            .map(id -> Notification.of(
                Member.ofId(id),
                e.winnerNickname(),
                NotificationType.COMPETITION,
                false))
            .toList();

        // 2) 저장
        notificationRepository.batchInsert(batch);

        // 3) SSE push
        sse.sendAll();
    }
}
