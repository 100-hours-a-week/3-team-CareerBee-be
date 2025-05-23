package org.choon.careerbee.domain.auth.service.oauth;

import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;

public interface OAuthApiClient {

    /***
     * OAuth요청을 위한 Client 인터페이스
     * oauthProvider() -> client의 타입 반환
     * requestAccessToken() -> AuthorizationCode를 기반으로 인증 API를 요청해서 AccessToken을 획득
     * requestOauthInfo() -> AccessToken을 이용하여 회원번호, Email 포함된 프로필 정보를 획득
     */
    OAuthProvider oauthProvider();

    String requestAccessToken(OAuthLoginParams loginParams, String origin);

    OAuthInfoResponse requestOauthInfo(String accessToken);
}
