package org.choon.careerbee.domain.notification.dto.request;

import java.util.List;

public record ReadNotificationReq(
    List<Long> notificationIds
) {

}
