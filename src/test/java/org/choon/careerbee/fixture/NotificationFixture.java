package org.choon.careerbee.fixture;

import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;

public class NotificationFixture {

    public static Notification createNotification(
        Member member, String content, NotificationType type, Boolean isRead
    ) {
        return Notification.of(
            member, content, type, isRead
        );
    }

}
