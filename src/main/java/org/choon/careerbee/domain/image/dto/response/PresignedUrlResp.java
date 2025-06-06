package org.choon.careerbee.domain.image.dto.response;

public record PresignedUrlResp(
    String uploadUrl,
    String objectUrl,
    long expiresIn
) {

}
