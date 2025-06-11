package org.choon.careerbee.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;

public record FetchNotiResp(
    List<NotificationInfo> notifications,
    Long nextCursor,
    boolean hasNext
) {

    public record NotificationInfo(
        Long id,
        NotificationType type,
        String content,
        LocalDateTime notiDate,
        Boolean isRead
    ) {

    }
}
