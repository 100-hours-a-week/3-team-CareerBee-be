package org.choon.careerbee.domain.member.dto.internal;

public record AdvancedResumeRespFromAi(
    Long memberId,
    boolean isComplete,
    String question,
    String resumeObjectKey
) {

}
