package org.choon.careerbee.domain.auth.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProviderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private OAuthLoginUrlProvider kakaoProvider;

    @Mock
    private OAuthLoginUrlProviderFactory providerFactory;


    @Test
    @DisplayName("소셜 로그인 URL 조회 성공 - 카카오")
    void getOAuthLoginUrl_kakao_success() {
        // given
        String origin = "http://localhost:5173";
        String expectedLoginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=test-client-id&redirect_uri=http://localhost:5173/oauth/callback";

        when(kakaoProvider.getLoginUrlByOrigin(origin)).thenReturn(expectedLoginUrl);
        when(providerFactory.getProvider(OAuthProvider.KAKAO)).thenReturn(kakaoProvider);

        // when
        OAuthLoginUrlResp actual = authService.getOAuthLoginUrl("kakao", origin);

        // then
        assertThat(actual.loginUrl()).isEqualTo(expectedLoginUrl);
    }

    @Test
    @DisplayName("소셜 로그인 URL 조회 실패 - 유효하지 않은 provider를 입력시 404 예외 발생")
    void getOAuthLoginUrl_shouldReturn404_invalidOAuthProvider() {
        // given
        String invalidOAuthProvider = "Daum";
        String origin = "http://localhost:5173";

        // when & then
        assertThatThrownBy(() -> authService.getOAuthLoginUrl(invalidOAuthProvider, origin))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.OAUTH_PROVIDER_NOT_EXIST.getMessage());

        verifyNoInteractions(kakaoProvider);
        verifyNoInteractions(providerFactory);
    }
}
