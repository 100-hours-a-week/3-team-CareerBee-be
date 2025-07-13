package org.choon.careerbee.domain.interview.dto.response;

public record AiFeedbackRespWrapper(
    int httpStatusCode,
    String message,
    AiFeedbackResp data
) {

}
