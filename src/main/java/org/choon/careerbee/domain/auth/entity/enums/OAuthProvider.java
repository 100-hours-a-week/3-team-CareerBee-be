package org.choon.careerbee.domain.auth.entity.enums;

import java.util.Arrays;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;

public enum OAuthProvider {
  KAKAO,
  NAVER,
  GOOGLE,;

  public static OAuthProvider fromString(String value) {
    return Arrays.stream(values())
        .filter(p -> p.name().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new CustomException(CustomResponseStatus.OAUTH_PROVIDER_NOT_EXIST));
  }
}
