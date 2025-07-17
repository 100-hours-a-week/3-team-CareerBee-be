package org.choon.careerbee.common.pubsub.dto;

import org.choon.careerbee.common.pubsub.enums.EventName;

public record AiErrorEvent(
    Long memberId,
    EventName eventName,
    String message
) {

    public static AiErrorEvent of(Long memberId, EventName eventName, String message) {
        return new AiErrorEvent(memberId, eventName, message);
    }

}
