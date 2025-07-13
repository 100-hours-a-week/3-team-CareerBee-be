package org.choon.careerbee.domain.interview.dto.request;

public record SubmitAnswerReq(
    Long problemId,
    String question,
    String answer,
    boolean isFreeProblem
) {

}
