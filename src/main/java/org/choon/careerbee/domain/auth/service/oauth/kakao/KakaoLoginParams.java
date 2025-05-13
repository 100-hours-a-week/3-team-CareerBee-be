package org.choon.careerbee.domain.auth.service.oauth.kakao;

import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginParams;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KakaoLoginParams implements OAuthLoginParams {

    /***
     * 카카오 API 요청에 필요한 authorizationCode를 갖고있는 클래스이다.
     */
    private String authorizationCode;

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    @Override
    public OAuthProvider oauthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        return body;
    }
}
