package org.choon.careerbee.domain.auth.service.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.springframework.stereotype.Component;

@Component
public class RequestOAuthInfoService {

    /***
     * OAuthApiClinet 사용하는 Service 클래스
     * KakaoApiClient와 NaverApiClient를 직접 주입받아 사용하면 중복되는 코드가 많아짐
     * List<OauthApiClient>를 주입받아서 Map으로 만들어두면 간단하게 사용가능함
     * 참고로 List<인터페이스>를 주입받으면 해당 인터페이스의 구현체들이 모두 List에 담겨온다.
     */
    private final Map<OAuthProvider, OAuthApiClient> clients;

    public RequestOAuthInfoService(List<OAuthApiClient> clients) {
        this.clients = clients.stream().collect(
            Collectors.toUnmodifiableMap(OAuthApiClient::oauthProvider, Function.identity())
        );
    }

    public OAuthInfoResponse request(OAuthLoginParams params, String origin) {
        OAuthApiClient client = clients.get(params.oauthProvider());
        String accessToken = client.requestAccessToken(params, origin);
        return client.requestOauthInfo(accessToken);
    }
}
