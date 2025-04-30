package org.choon.careerbee.domain.auth.dto.response;

import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.response.LoginResp.UserInfo;

public record TokenAndUserInfo(
    AuthTokens authTokens,
    UserInfo userInfo
) {

}
