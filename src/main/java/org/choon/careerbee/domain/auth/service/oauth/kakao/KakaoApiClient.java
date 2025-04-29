package org.choon.careerbee.domain.auth.service.oauth.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthApiClient;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthApiClient {
    private static final String GRANT_TYPE = "authorization_code";
    @Value("${oauth.kakao.auth-uri}")
    private String authUrl;

    @Value("${oauth.kakao.api-uri}")
    private String apiUrl;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

//    private final RestTemplate restTemplate;

    @Override
    public OAuthProvider oauthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public String requestAccessToken(OAuthLoginParams loginParams) {
        String url = authUrl + "/oauth/token";
        log.info("[URL] : "+url);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = loginParams.makeBody();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        body.add("redirect_uri", "http://localhost:8080/kakao/callback");

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

//        KakaoTokens response = restTemplate.postForObject(url, request, KakaoTokens.class);
//
//        Objects.requireNonNull(response);
//        return response.getAccessToken();
        return null;
    }

    @Override
    public OAuthInfoResponse requestOauthInfo(String accessToken) {
        String url = apiUrl + "/v2/user/me";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("property_keys", "[\"kakao_account.email\", \"kakao_account.profile\"]");

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

//        return restTemplate.postForObject(url, request, KakaoInfoResponse.class);
        return null;
    }
}
