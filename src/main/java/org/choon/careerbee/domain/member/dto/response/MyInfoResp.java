package org.choon.careerbee.domain.member.dto.response;

public record MyInfoResp(
    String nickname,
    String email,
    String profileUrl,
    boolean hasNewAlarm,
    int point
) {

}
