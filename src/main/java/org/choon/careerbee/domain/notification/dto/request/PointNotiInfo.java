package org.choon.careerbee.domain.notification.dto.request;

import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;

public record PointNotiInfo(
    Member member,
    int point,
    NotificationType type,
    boolean isRead
) {

}
