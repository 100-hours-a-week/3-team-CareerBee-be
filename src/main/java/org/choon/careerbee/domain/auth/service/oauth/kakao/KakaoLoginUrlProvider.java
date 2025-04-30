package org.choon.careerbee.domain.auth.service.oauth.kakao;

import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoLoginUrlProvider implements OAuthLoginUrlProvider {

  @Value("${oauth.kakao.client-id}")
  private String clientId;

  @Value("${oauth.kakao.redirect-uri}")
  private String redirectUri;

  @Value("${oauth.kakao.auth-uri}")
  private String authUri;

  @Override
  public OAuthProvider getOAuthProvider() {
    return OAuthProvider.KAKAO;
  }

  @Override
  public String getLoginUrl() {
    return String.format("%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
        authUri, clientId, redirectUri);
  }
}
