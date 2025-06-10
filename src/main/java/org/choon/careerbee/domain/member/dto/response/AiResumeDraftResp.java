package org.choon.careerbee.domain.member.dto.response;

public record AiResumeDraftResp<T>(
    int httpStatusCode,
    String message,
    T data
) {

}
