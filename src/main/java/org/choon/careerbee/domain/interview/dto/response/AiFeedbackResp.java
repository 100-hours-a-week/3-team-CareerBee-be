package org.choon.careerbee.domain.interview.dto.response;

public record AiFeedbackResp(
    Long problemId,
    String feedback
) {

    public static AiFeedbackResp of(Long problemId, AiFeedbackRespFromAi aiFeedbackRespFromAi) {
        return new AiFeedbackResp(
            problemId, aiFeedbackRespFromAi.feedback()
        );
    }
}
