package org.choon.careerbee.domain.auth.service.oauth;

import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;

public interface OAuthLoginUrlProvider {
  OAuthProvider getOAuthProvider();
  String getLoginUrl();
}
