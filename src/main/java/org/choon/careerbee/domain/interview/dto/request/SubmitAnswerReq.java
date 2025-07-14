package org.choon.careerbee.domain.interview.dto.request;

import org.choon.careerbee.domain.interview.domain.enums.ProblemType;

public record SubmitAnswerReq(
    Long problemId,
    ProblemType type,
    String question,
    String answer,
    boolean isFreeProblem
) {

}
