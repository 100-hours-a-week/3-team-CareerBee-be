package org.choon.careerbee.domain.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.NotificationFixture.createNotification;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.notification.dto.request.ReadNotificationReq;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.choon.careerbee.domain.notification.service.NotificationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationCommandService notificationCommandService;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("알림 읽음 처리 - 실제 DB에 isRead = true로 반영된다")
    void markAsRead_shouldUpdateIsReadFieldInDatabase() {
        // given
        Member member = memberRepository.save(createMember("통합테스트", "integration@test.com", 1000L));
        Notification n1 = notificationRepository.save(
            createNotification(member, "알림1", NotificationType.POINT, false));
        Notification n2 = notificationRepository.save(
            createNotification(member, "알림2", NotificationType.POINT, false));

        ReadNotificationReq req = new ReadNotificationReq(List.of(n1.getId(), n2.getId()));

        // when
        notificationCommandService.markAsRead(member.getId(), req);
        em.flush();
        em.clear();

        // then
        Notification updated1 = notificationRepository.findById(n1.getId()).orElseThrow();
        Notification updated2 = notificationRepository.findById(n2.getId()).orElseThrow();

        assertThat(updated1.getIsRead()).isTrue();
        assertThat(updated2.getIsRead()).isTrue();
    }
}
