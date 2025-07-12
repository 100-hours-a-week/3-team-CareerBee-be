package org.choon.careerbee.domain.auth.service.oauth.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthApiClient;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

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

    @Value("${oauth.kakao.prod-redirect-uri}")
    private String prodRedirectUri;

    @Value("${oauth.kakao.dev-redirect-uri}")
    private String devRedirectUri;

    @Value("${oauth.kakao.local-redirect-uri}")
    private String localRedirectUri;

    @Value("${oauth.kakao.next-local-redirect-uri}")
    private String nextLocalRedirectUri;

    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider oauthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public String requestAccessToken(OAuthLoginParams loginParams, String origin) {
        String url = authUrl + "/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = loginParams.makeBody();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        body.add("redirect_uri", getRedirectUriByOrigin(origin));

        KakaoTokens response = restClient.post()
            .uri(url)
            .headers(h -> h.addAll(headers))
            .body(body)
            .retrieve()
            .body(KakaoTokens.class);

        return response.getAccessToken();
    }

    @Override
    public OAuthInfoResponse requestOauthInfo(String accessToken) {
        String url = apiUrl + "/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);

        String jsonBody = """
            {
                "property_keys": [
                    "kakao_account.email",
                ]
            }
            """;

        return restClient.post()
            .uri(url)
            .headers(h -> h.addAll(headers))
            .body(jsonBody)
            .retrieve()
            .body(KakaoInfoResponse.class);
    }

    private String getRedirectUriByOrigin(String origin) {
        if (origin == null) {
            return prodRedirectUri;
        }

        log.info("[로그인] origin: {}", origin);
        String redirectUri = switch (origin) {
            case "http://localhost:5173" -> localRedirectUri;
            case "https://localhost:5173" -> nextLocalRedirectUri;
            case "https://www.dev.careerbee.co.kr" -> devRedirectUri;
            default -> prodRedirectUri;
        };
        log.info("[로그인] redirect uri : {}", redirectUri);

        return redirectUri;
    }
}
