package org.choon.careerbee.common.pubsub.dto;

import org.choon.careerbee.domain.member.dto.response.AdvancedResumeInitResp;

public record AdvancedResumeInitEvent(
    Long memberId,
    AdvancedResumeInitResp result
) {

}
