package org.choon.careerbee.domain.auth.service.auth;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProviderFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final OAuthLoginUrlProviderFactory providerFactory;

  @Override
  public OAuthLoginUrlResp getOAuthLoginUrl(String oauthProvider) {
    OAuthProvider provider = OAuthProvider.fromString(oauthProvider);

    return new OAuthLoginUrlResp(providerFactory.getProvider(provider).getLoginUrl());
  }
}
