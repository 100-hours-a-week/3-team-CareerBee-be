package org.choon.careerbee.common.pubsub.dto;

import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;

public record ResumeExtractedEvent(
    Long memberId,
    ExtractResumeResp result
) {

}
