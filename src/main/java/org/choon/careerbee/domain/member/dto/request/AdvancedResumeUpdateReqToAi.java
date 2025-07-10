package org.choon.careerbee.domain.member.dto.request;

public record AdvancedResumeUpdateReqToAi(
    Long memberId,
    MemberInput inputs
) {

    public record MemberInput(
        String answer
    ) {

    }

    public static AdvancedResumeUpdateReqToAi of(
        Long memberId, String answer
    ) {
        return new AdvancedResumeUpdateReqToAi(memberId, new MemberInput(answer));
    }
}
