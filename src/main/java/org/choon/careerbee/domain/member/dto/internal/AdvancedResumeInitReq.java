package org.choon.careerbee.domain.member.dto.internal;

import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;

public record AdvancedResumeInitReq(
    Long memberId,
    ResumeDraftReq inputs
) {

}
