package org.choon.careerbee.domain.auth.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.TokenFixture.createToken;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.jwt.TokenClaimInfo;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
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
import org.choon.careerbee.util.jwt.TokenGenerator;
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
    private JwtUtil jwtUtil;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;
    @Mock
    private RequestOAuthInfoService requestOAuthInfoService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberCommandService memberCommandService;

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

    @DisplayName("로그인 성공 – 기존 회원 & 이전 LIVE 토큰 있음 → 새 RT 발급 + 기존 RT REVOKE")
    @Test
    void login_existingMember_issueNewRefreshToken() {
        // given
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "code123");

        KakaoInfoResponse kakaoInfo = new KakaoInfoResponse();
        KakaoAccount account = new KakaoAccount();
        ReflectionTestUtils.setField(account, "email", "old@kakao.com");
        ReflectionTestUtils.setField(kakaoInfo, "id", 321L);
        ReflectionTestUtils.setField(kakaoInfo, "kakaoAccount", account);

        Member member = createMember("oldnick", "old@kakao.com", 321L);
        ReflectionTestUtils.setField(member, "id", 10L);

        Token oldLiveRt = createToken(member, "old-refresh", TokenStatus.LIVE);

        when(requestOAuthInfoService.request(params, "http://localhost:5173")).thenReturn(
            kakaoInfo);
        when(memberQueryService.findMemberByProviderId(321L)).thenReturn(Optional.of(member));
        when(tokenRepository.findByMemberAndStatus(member, TokenStatus.LIVE))
            .thenReturn(Optional.of(oldLiveRt));

        when(jwtUtil.createToken(10L, TokenType.ACCESS_TOKEN)).thenReturn("new-access");
        when(jwtUtil.createToken(10L, TokenType.REFRESH_TOKEN)).thenReturn("new-refresh");

        // when
        AuthTokens result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        assertThat(oldLiveRt.getStatus()).isEqualTo(TokenStatus.REVOKED);

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());

        Token saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getStatus()).isEqualTo(TokenStatus.LIVE);
        assertThat(saved.getTokenValue()).isEqualTo("new-refresh");
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
        ReflectionTestUtils.setField(oAuthInfo, "id", 999L);
        ReflectionTestUtils.setField(oAuthInfo, "kakaoAccount", kakaoAccount);

        Member newMember = createMember("newnick", "new@kakao.com", 999L);
        ReflectionTestUtils.setField(newMember, "id", 2L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173")).thenReturn(
            oAuthInfo);
        when(memberQueryService.findMemberByProviderId(999L)).thenReturn(Optional.empty());
        when(memberCommandService.forceJoin(oAuthInfo)).thenReturn(newMember);
        when(jwtUtil.createToken(newMember.getId(), TokenType.ACCESS_TOKEN)).thenReturn(
            "access-token-new");
        when(jwtUtil.createToken(newMember.getId(), TokenType.REFRESH_TOKEN)).thenReturn(
            "refresh-token-new");
        when(tokenRepository.findByMemberAndStatus(newMember, TokenStatus.LIVE)).thenReturn(
            Optional.empty());

        // when
        AuthTokens result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.accessToken()).isEqualTo("access-token-new");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-new");

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        Token savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getMember().getId()).isEqualTo(2L);
        assertThat(savedToken.getStatus()).isEqualTo(TokenStatus.LIVE);
        assertThat(savedToken.getTokenValue()).isEqualTo("refresh-token-new");
    }

    @DisplayName("로그인 성공 – 리프레시 토큰이 없던 기존 회원 → 새 RT 발급·저장")
    @Test
    void login_existingMember_withoutRefreshToken() {
        // given
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "code456");

        KakaoInfoResponse kakaoInfo = new KakaoInfoResponse();
        KakaoAccount account = new KakaoAccount();
        ReflectionTestUtils.setField(account, "email", "exist@kakao.com");
        ReflectionTestUtils.setField(kakaoInfo, "id", 654L);
        ReflectionTestUtils.setField(kakaoInfo, "kakaoAccount", account);

        Member member = createMember("existnick", "exist@kakao.com", 654L);
        ReflectionTestUtils.setField(member, "id", 20L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173")).thenReturn(
            kakaoInfo);
        when(memberQueryService.findMemberByProviderId(654L)).thenReturn(Optional.of(member));
        when(tokenRepository.findByMemberAndStatus(member, TokenStatus.LIVE)).thenReturn(
            Optional.empty());

        when(jwtUtil.createToken(20L, TokenType.ACCESS_TOKEN)).thenReturn("access-exist");
        when(jwtUtil.createToken(20L, TokenType.REFRESH_TOKEN)).thenReturn("refresh-exist");

        // when
        AuthTokens result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.accessToken()).isEqualTo("access-exist");
        assertThat(result.refreshToken()).isEqualTo("refresh-exist");

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());

        Token saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getStatus()).isEqualTo(TokenStatus.LIVE);
        assertThat(saved.getTokenValue()).isEqualTo("refresh-exist");
    }

    @DisplayName("로그인 실패 – 탈퇴한 회원인 경우 410 예외 발생")
    @Test
    void login_withdrawnMember_shouldReturn410() {
        // given
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "code456");

        KakaoInfoResponse kakaoInfo = new KakaoInfoResponse();
        KakaoAccount account = new KakaoAccount();
        ReflectionTestUtils.setField(account, "email", "exist@kakao.com");
        ReflectionTestUtils.setField(kakaoInfo, "id", 654L);
        ReflectionTestUtils.setField(kakaoInfo, "kakaoAccount", account);

        Member member = createMember("existnick", "exist@kakao.com", 654L);
        ReflectionTestUtils.setField(member, "id", 20L);
        ReflectionTestUtils.setField(member, "withdrawnAt", LocalDateTime.now());

        when(requestOAuthInfoService.request(params, "http://localhost:5173")).thenReturn(
            kakaoInfo);
        when(memberQueryService.findMemberByProviderId(654L)).thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> authService.login(params, "http://localhost:5173"))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.WITHDRAWAL_MEMBER.getMessage());
    }

    @Test
    @DisplayName("로그아웃 성공 - 토큰 블랙리스트 처리 및 기존 리프레시 토큰 로그아웃")
    void logout_success() {
        // given
        Member member = createMember("nickname", "email@test.com", 1L);
        ReflectionTestUtils.setField(member, "id", 1L);

        String accessToken = "Bearer some-valid-token";
        String resolvedToken = "some-valid-token";
        TokenClaimInfo tokenClaims = new TokenClaimInfo(member.getId());
        Token refreshToken = new Token(member, TokenStatus.LIVE, "refresh-token");

        when(jwtUtil.resolveToken(anyString())).thenReturn(resolvedToken);
        when(jwtUtil.getTokenClaims(anyString())).thenReturn(tokenClaims);
        when(memberQueryService.getReferenceById(anyLong())).thenReturn(member);
        when(tokenRepository.findByMemberIdAndStatus(anyLong(), any(TokenStatus.class)))
            .thenReturn(Optional.of(refreshToken));

        // when
        authService.logout(accessToken);

        // then
        assertThat(refreshToken.getStatus()).isEqualTo(TokenStatus.BLACK);
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        Token savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getMember().getId()).isEqualTo(1L);
        assertThat(savedToken.getStatus()).isEqualTo(TokenStatus.BLACK);
        assertThat(savedToken.getTokenValue()).isEqualTo(resolvedToken);
    }

    @Test
    @DisplayName("로그아웃 실패 - 토큰의 ID에 해당하는 멤버가 존재하지 않는 경우")
    void logout_shouldThrow_whenMemberNotFound() {
        // given
        String accessToken = "Bearer some-valid-token";
        String resolvedToken = "some-valid-token";
        TokenClaimInfo tokenClaims = new TokenClaimInfo(99L);

        when(jwtUtil.resolveToken(anyString())).thenReturn(resolvedToken);
        when(jwtUtil.getTokenClaims(anyString())).thenReturn(tokenClaims);
        when(memberQueryService.getReferenceById(anyLong()))
            .thenThrow(new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        // when & then
        assertThatThrownBy(() -> authService.logout(accessToken))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_success() {
        // given
        Long memberId = 1L;
        Member member = createMember("nickname", "email@test.com", memberId);
        ReflectionTestUtils.setField(member, "id", 1L);

        String oldRefreshToken = "old-refresh-token";
        TokenClaimInfo tokenClaimInfo = new TokenClaimInfo(memberId);
        Token storedToken = createToken(member, oldRefreshToken, TokenStatus.LIVE);
        AuthTokens newAuthTokens = new AuthTokens("new-access-token", "new-refresh-token");

        when(jwtUtil.getTokenClaims(oldRefreshToken)).thenReturn(tokenClaimInfo);
        when(memberQueryService.getReferenceById(anyLong())).thenReturn(member);
        when(tokenRepository.findByTokenValueAndStatus(anyString(), any(TokenStatus.class)))
            .thenReturn(Optional.of(storedToken));
        when(tokenGenerator.generateToken(anyLong())).thenReturn(newAuthTokens);

        // when
        AuthTokens result = authService.reissue(oldRefreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("재발급 실패 - 리프레시 토큰 만료")
    void reissue_shouldThrow_whenTokenExpired() {
        // given
        String expiredRefreshToken = "expired-token";
        when(jwtUtil.getTokenClaims(expiredRefreshToken)).thenThrow(
            new ExpiredJwtException(null, null, "만료"));

        // when & then
        assertThatThrownBy(() -> authService.reissue(expiredRefreshToken))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.REFRESH_TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("재발급 실패 - 저장된 리프레시 토큰이 없음")
    void reissue_shouldThrow_whenNoStoredRefreshToken() {
        // given
        Member member = createMember("nickname", "email@test.com", 1234L);
        ReflectionTestUtils.setField(member, "id", 1L);

        String refreshToken = "refresh-token";
        TokenClaimInfo tokenClaimInfo = new TokenClaimInfo(member.getId());

        when(jwtUtil.getTokenClaims(anyString())).thenReturn(tokenClaimInfo);
        when(memberQueryService.getReferenceById(anyLong())).thenReturn(member);
        when(tokenRepository.findByTokenValueAndStatus(anyString(), any(TokenStatus.class)))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }
}
