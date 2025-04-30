package org.choon.careerbee.domain.auth.service.auth;

import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.dto.response.TokenAndUserInfo;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginParams;

public interface AuthService {

  OAuthLoginUrlResp getOAuthLoginUrl(String oauthProvider);

  TokenAndUserInfo login(OAuthLoginParams oAuthLoginParams);

  void logout(String accessToken);

  AuthTokens reissue(String refreshToken);
}
