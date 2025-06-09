package org.choon.careerbee.domain.member.dto.request;

public record UpdateProfileCommand(
    String profileImgUrl,
    String email,
    String nickname
) {
}
