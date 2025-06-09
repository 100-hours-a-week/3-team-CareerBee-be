package org.choon.careerbee.domain.member.dto.response;

public record AiResumeExtractResp<T>(
    String message,
    T data
) {

}
