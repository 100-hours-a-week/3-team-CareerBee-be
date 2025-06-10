package org.choon.careerbee.domain.member.dto.request;

public record UpdateProfileInfoReq(
    String newProfileUrl,
    String newNickname
) {

}
