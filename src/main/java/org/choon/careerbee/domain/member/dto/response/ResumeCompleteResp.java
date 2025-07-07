package org.choon.careerbee.domain.member.dto.response;

public record ResumeCompleteResp(
    Long memberId,
    boolean isComplete,
    String resumeObjectKey
) implements AdvancedResumeResp {

}
