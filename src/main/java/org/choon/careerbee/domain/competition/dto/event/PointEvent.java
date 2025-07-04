package org.choon.careerbee.domain.competition.dto.event;

import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;

public record PointEvent(
    Member member,
    int point,
    NotificationType type,
    boolean isRead
) {

}
