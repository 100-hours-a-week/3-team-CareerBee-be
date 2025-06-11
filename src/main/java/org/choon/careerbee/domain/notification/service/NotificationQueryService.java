package org.choon.careerbee.domain.notification.service;

import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;

public interface NotificationQueryService {

    FetchNotiResp fetchMemberNotifications(Long accessMemberId, Long cursor, int size);
}
