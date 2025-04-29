package org.choon.careerbee.domain.auth.service.auth;

import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;

public interface AuthService {

  OAuthLoginUrlResp getOAuthLoginUrl(String oauthProvider);
}
