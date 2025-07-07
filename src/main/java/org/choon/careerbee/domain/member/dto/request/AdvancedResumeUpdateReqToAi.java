package org.choon.careerbee.domain.member.dto.request;

public record AdvancedResumeUpdateReqToAi(
    Long memberId,
    String answer
) {

    public static AdvancedResumeUpdateReqToAi of(
        Long memberId, String answer
    ) {
        return new AdvancedResumeUpdateReqToAi(memberId, answer);
    }
}
