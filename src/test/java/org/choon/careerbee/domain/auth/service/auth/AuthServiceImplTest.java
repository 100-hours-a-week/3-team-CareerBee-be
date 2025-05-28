package org.choon.careerbee.domain.auth.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.dto.response.TokenAndUserInfo;
import org.choon.careerbee.domain.auth.entity.Token;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.entity.enums.TokenStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.auth.repository.TokenRepository;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProviderFactory;
import org.choon.careerbee.domain.auth.service.oauth.RequestOAuthInfoService;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoInfoResponse;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoInfoResponse.KakaoAccount;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoLoginParams;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.member.service.MemberCommandService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private OAuthLoginUrlProvider kakaoProvider;

    @Mock
    private OAuthLoginUrlProviderFactory providerFactory;

    @Mock
    private RequestOAuthInfoService requestOAuthInfoService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberCommandService memberCommandService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private MemberQueryService memberQueryService;

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

    @Test
    @DisplayName("로그인 성공 - 기존 회원 & 기존 리프레시 토큰 있음")
    void login_existingMember_existingRefreshToken() {
        // given
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "test-code");

        KakaoInfoResponse oAuthInfo = new KakaoInfoResponse();
        KakaoAccount kakaoAccount = new KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "test@kakao.com");
        ReflectionTestUtils.setField(oAuthInfo, "kakaoAccount", kakaoAccount);

        Member member = createMember("testnick", "test@test.com", 123L);
        ReflectionTestUtils.setField(member, "id", 1L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173"))
            .thenReturn(oAuthInfo);
        when(memberRepository.findByEmail("test@kakao.com")).thenReturn(Optional.of(member));
        when(jwtUtil.createToken(1L, TokenType.ACCESS_TOKEN)).thenReturn("access-token");
        when(tokenRepository.findByMemberAndStatus(member, TokenStatus.LIVE))
            .thenReturn(Optional.of(new Token(member, TokenStatus.LIVE, "refresh-token")));

        // when
        TokenAndUserInfo result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.authTokens().accessToken()).isEqualTo("access-token");
        assertThat(result.authTokens().refreshToken()).isEqualTo("refresh-token");
        assertThat(result.userInfo().userPoint()).isEqualTo(0);
        assertThat(result.userInfo().hasNewAlarm()).isFalse();

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<TokenStatus> statusCaptor = ArgumentCaptor.forClass(TokenStatus.class);
        verify(tokenRepository).findByMemberAndStatus(
            memberCaptor.capture(),
            statusCaptor.capture()
        );

        assertThat(memberCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(statusCaptor.getValue()).isEqualTo(TokenStatus.LIVE);
    }


    @Test
    @DisplayName("로그인 성공 - 신규 회원 강제가입 & 리프레시 토큰 없음")
    void login_newMember_noRefreshToken() {
        // given
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "new-code");

        KakaoInfoResponse oAuthInfo = new KakaoInfoResponse();
        KakaoAccount kakaoAccount = new KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "new@kakao.com");
        ReflectionTestUtils.setField(oAuthInfo, "kakaoAccount", kakaoAccount);

        Member newMember = createMember("newnick", "new@kakao.com", 999L);
        ReflectionTestUtils.setField(newMember, "id", 2L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173")).thenReturn(
            oAuthInfo);
        when(memberRepository.findByEmail("new@kakao.com")).thenReturn(Optional.empty());
        when(memberCommandService.forceJoin(oAuthInfo)).thenReturn(newMember);
        when(jwtUtil.createToken(newMember.getId(), TokenType.ACCESS_TOKEN)).thenReturn(
            "access-token-new");
        when(jwtUtil.createToken(newMember.getId(), TokenType.REFRESH_TOKEN)).thenReturn(
            "refresh-token-new");
        when(tokenRepository.findByMemberAndStatus(newMember, TokenStatus.LIVE)).thenReturn(
            Optional.empty());

        // when
        TokenAndUserInfo result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.authTokens().accessToken()).isEqualTo("access-token-new");
        assertThat(result.authTokens().refreshToken()).isEqualTo("refresh-token-new");

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        Token savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getMember().getId()).isEqualTo(2L);
        assertThat(savedToken.getStatus()).isEqualTo(TokenStatus.LIVE);
        assertThat(savedToken.getTokenValue()).isEqualTo("refresh-token-new");
    }

    @Test
    @DisplayName("로그인 성공 - 기존 회원 & 리프레시 토큰 없음 → 새로 발급 후 저장")
    void login_existingMember_noRefreshToken() {
        // given
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "existing-code");

        KakaoInfoResponse oAuthInfo = new KakaoInfoResponse();
        KakaoAccount kakaoAccount = new KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "exist@kakao.com");
        ReflectionTestUtils.setField(oAuthInfo, "kakaoAccount", kakaoAccount);

        Member member = createMember("existnick", "exist@kakao.com", 777L);
        ReflectionTestUtils.setField(member, "id", 3L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173")).thenReturn(
            oAuthInfo);
        when(memberRepository.findByEmail("exist@kakao.com")).thenReturn(Optional.of(member));
        when(jwtUtil.createToken(3L, TokenType.ACCESS_TOKEN)).thenReturn("access-token-exist");
        when(jwtUtil.createToken(3L, TokenType.REFRESH_TOKEN)).thenReturn("refresh-token-exist");
        when(tokenRepository.findByMemberAndStatus(member, TokenStatus.LIVE)).thenReturn(
            Optional.empty());

        // when
        TokenAndUserInfo result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.authTokens().accessToken()).isEqualTo("access-token-exist");
        assertThat(result.authTokens().refreshToken()).isEqualTo("refresh-token-exist");

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        Token savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getMember().getId()).isEqualTo(member.getId());
        assertThat(savedToken.getStatus()).isEqualTo(TokenStatus.LIVE);
        assertThat(savedToken.getTokenValue()).isEqualTo("refresh-token-exist");
    }
}
