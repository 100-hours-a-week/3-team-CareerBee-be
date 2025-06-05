package org.choon.careerbee.domain.member.dto.response;

public record MyInfoResp(
    String nickname,
    String profileUrl,
    String badgeUrl,
    String frameUrl,
    boolean hasNewAlarm,
    int point
) {

}
