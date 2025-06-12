package org.choon.careerbee.domain.notification.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.NotificationFixture.createNotification;

import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import({QueryDSLConfig.class, NotificationCustomRepositoryImpl.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class NotificationCustomRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("회원 알림 목록 조회 - 첫 페이지에서는 default size만큼 가져와짐")
    void fetchNotifications_firstPage() {
        // given
        Member member = memberRepository.save(createMember("notiUser", "noti@test.com", 88L));
        for (int i = 0; i < 7; i++) {
            Notification notification = createNotification(
                member,
                "알림 내용 " + i,
                NotificationType.POINT,
                false
            );
            notificationRepository.save(notification);
        }

        // when
        FetchNotiResp result = notificationRepository.fetchNotificationsByMemberId(member.getId(),
            null, 5);

        // then
        assertThat(result.notifications()).hasSize(5);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("회원 알림 목록 조회 - cursor 기반 페이지네이션")
    void fetchNotifications_withCursor() {
        // given
        Member member = memberRepository.save(createMember("pageUser", "page@test.com", 99L));

        // 최신순으로 id 높은 게 먼저 나올 것이므로, 일부러 id를 낮게 만드는 순서로 저장
        for (int i = 0; i < 6; i++) {
            notificationRepository.save(createNotification(
                member, "채용 알림 " + i, NotificationType.RECRUITMENT, false
            ));
        }

        FetchNotiResp firstPage = notificationRepository.fetchNotificationsByMemberId(
            member.getId(), null, 3);
        Long cursor = firstPage.nextCursor();

        // when
        FetchNotiResp secondPage = notificationRepository.fetchNotificationsByMemberId(
            member.getId(), cursor, 3);

        // then
        assertThat(secondPage.notifications()).hasSize(3);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.notifications().get(0).id()).isLessThan(cursor);
    }
}
