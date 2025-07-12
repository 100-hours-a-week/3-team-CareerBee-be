package org.choon.careerbee.domain.auth.service.oauth.kakao;

import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KakaoLoginUrlProvider implements OAuthLoginUrlProvider {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.prod-redirect-uri}")
    private String prodRedirectUri;

    @Value("${oauth.kakao.dev-redirect-uri}")
    private String devRedirectUri;

    @Value("${oauth.kakao.local-redirect-uri}")
    private String localRedirectUri;

    @Value("${oauth.kakao.next-local-redirect-uri}")
    private String nextLocalRedirectUri;

    @Value("${oauth.kakao.auth-uri}")
    private String authUri;

    @Override
    public OAuthProvider getOAuthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public String getLoginUrlByOrigin(String origin) {
        log.info("[로그인 페이지] origin : {}", origin);

        String redirectUri = switch (origin) {
            case "http://localhost:5173" -> localRedirectUri;
            case "https://localhost:5173" -> nextLocalRedirectUri;
            case "https://www.dev.careerbee.co.kr" -> devRedirectUri;
            default -> prodRedirectUri;
        };

        log.info("[로그인 페이지] redirect uri : {}", redirectUri);

        return String.format(
            "%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
            authUri, clientId, redirectUri
        );
    }

}
