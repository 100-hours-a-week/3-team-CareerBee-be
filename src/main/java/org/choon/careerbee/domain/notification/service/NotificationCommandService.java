package org.choon.careerbee.domain.notification.service;

import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.choon.careerbee.domain.notification.dto.request.ReadNotificationReq;

public interface NotificationCommandService {

    void markAsRead(Long accessMemberId, ReadNotificationReq request);
    void saveNoti(PointEvent pointEvent);
}
