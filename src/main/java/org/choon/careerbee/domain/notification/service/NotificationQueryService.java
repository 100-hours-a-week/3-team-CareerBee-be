package org.choon.careerbee.domain.notification.service;

import java.util.List;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;
import org.choon.careerbee.domain.notification.entity.Notification;

public interface NotificationQueryService {

    FetchNotiResp fetchMemberNotifications(Long accessMemberId, Long cursor, int size);

    List<Notification> fetchNotificationInIds(List<Long> notificationIds, Long memberId);

}
