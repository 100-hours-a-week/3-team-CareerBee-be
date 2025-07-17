package org.choon.careerbee.common.pubsub.dto;

import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;

public record FeedbackEvent(
    Long memberId,
    AiFeedbackResp result
) {

}
