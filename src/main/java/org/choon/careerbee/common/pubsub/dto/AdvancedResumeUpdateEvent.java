package org.choon.careerbee.common.pubsub.dto;

import org.choon.careerbee.domain.member.dto.response.AdvancedResumeResp;

public record AdvancedResumeUpdateEvent(
    Long memberId,
    AdvancedResumeResp result
) {

}
