package org.choon.careerbee.domain.member.dto.request;

import jakarta.annotation.Nullable;

public record UpdateProfileInfoReq(
    @Nullable String newProfileUrl,
    String newNickname
) {

}
