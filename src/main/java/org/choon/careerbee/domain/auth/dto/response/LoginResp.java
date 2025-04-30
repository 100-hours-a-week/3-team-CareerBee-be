package org.choon.careerbee.domain.auth.dto.response;

public record LoginResp(
  String accessToken,
  UserInfo userInfo
) {

  public record UserInfo(
      Integer userPoint,
      boolean hasNewAlarm
  ) {}
}
