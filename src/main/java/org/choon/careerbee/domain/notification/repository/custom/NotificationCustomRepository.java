package org.choon.careerbee.domain.notification.repository.custom;

import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;

public interface NotificationCustomRepository {

    FetchNotiResp fetchNotificationsByMemberId(Long accessMemberId, Long cursor, int size);
}
