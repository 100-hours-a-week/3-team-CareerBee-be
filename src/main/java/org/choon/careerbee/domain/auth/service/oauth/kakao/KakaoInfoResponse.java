package org.choon.careerbee.domain.auth.service.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoInfoResponse implements OAuthInfoResponse {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        private String email;
    }

    @Override
    public String getEmail() {
        return kakaoAccount.email;
    }

    @Override
    public OAuthProvider getOauthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public Long getProviderId() {
        return id;
    }
}
