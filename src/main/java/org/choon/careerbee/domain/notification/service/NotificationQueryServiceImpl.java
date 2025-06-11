package org.choon.careerbee.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Override
    public FetchNotiResp fetchMemberNotifications(Long accessMemberId, Long cursor, int size) {
        return notificationRepository.fetchNotificationsByMemberId(accessMemberId, cursor, size);
    }
}
