package org.choon.careerbee.domain.interview.dto.request;

public record AiFeedbackReq(
    Long memberId,
    String question,
    String answer
) {

    public static AiFeedbackReq of(Long memberId, String question, String answer) {
        return new AiFeedbackReq(
            memberId, question, answer
        );
    }

}
