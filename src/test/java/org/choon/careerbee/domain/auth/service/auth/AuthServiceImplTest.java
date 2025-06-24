package org.choon.careerbee.domain.auth.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.jwt.TokenClaimInfo;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceImplTest {

    private static final String RT_KEY = "rt:";
    private static final String BL_KEY = "bl:";
    private static final Long RT_TTL = (long) (1000 * 600);

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
    private RedissonClient redissonClient;

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

    @DisplayName("로그인 성공 – 기존 회원, Redis RT 덮어쓰기")
    @Test
    void login_existingMember_overwriteRefreshTokenInRedis() {
        // given
        // (1) OAuth 파라미터 & 외부 API 응답
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "code123");

        KakaoInfoResponse kakaoInfo = new KakaoInfoResponse();
        KakaoAccount account = new KakaoAccount();
        ReflectionTestUtils.setField(account, "email", "old@kakao.com");
        ReflectionTestUtils.setField(kakaoInfo, "id", 321L);
        ReflectionTestUtils.setField(kakaoInfo, "kakaoAccount", account);
        ReflectionTestUtils.setField(authService, "refreshTokenTTL", 600_000L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173"))
            .thenReturn(kakaoInfo);

        // (2) 이미 가입된 멤버
        Member member = createMember("oldnick", "old@kakao.com", 321L);
        ReflectionTestUtils.setField(member, "id", 10L);
        when(memberQueryService.findMemberByProviderId(321L)).thenReturn(Optional.of(member));

        // (3) Redis 버킷 mock
        RBucket<String> rtBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(RT_KEY + member.getId())).thenReturn(rtBucket);

        // (4) 토큰 생성 mock
        when(jwtUtil.createToken(10L, TokenType.ACCESS_TOKEN)).thenReturn("new-access");
        when(jwtUtil.createToken(10L, TokenType.REFRESH_TOKEN)).thenReturn("new-refresh");

        // when
        AuthTokens result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");

        verify(rtBucket).set(eq("new-refresh"), any(Duration.class));
    }


    @DisplayName("로그인 성공 – 신규 회원 force-join, Redis RT 최초 저장")
    @Test
    void login_newMember_forceJoin_andSaveRefreshToken() {
        // given
        // (1) OAuth 파라미터 & 외부 API 응답
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "new-code");

        KakaoInfoResponse oAuthInfo = new KakaoInfoResponse();
        KakaoAccount kakaoAccount = new KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "new@kakao.com");
        ReflectionTestUtils.setField(oAuthInfo, "id", 999L);
        ReflectionTestUtils.setField(oAuthInfo, "kakaoAccount", kakaoAccount);
        ReflectionTestUtils.setField(authService, "refreshTokenTTL", 600_000L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173"))
            .thenReturn(oAuthInfo);

        // (2) 회원 조회 결과 없음  →  강제 가입
        when(memberQueryService.findMemberByProviderId(999L)).thenReturn(Optional.empty());

        Member newMember = createMember("newnick", "new@kakao.com", 999L);
        ReflectionTestUtils.setField(newMember, "id", 2L);

        when(memberCommandService.forceJoin(oAuthInfo)).thenReturn(newMember);

        // (3) 토큰 생성 mock
        when(jwtUtil.createToken(2L, TokenType.ACCESS_TOKEN)).thenReturn("access-token-new");
        when(jwtUtil.createToken(2L, TokenType.REFRESH_TOKEN)).thenReturn("refresh-token-new");

        // (4) Redis 버킷 mock
        RBucket<String> rtBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(RT_KEY + newMember.getId())).thenReturn(rtBucket);

        // when
        AuthTokens result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.accessToken()).isEqualTo("access-token-new");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-new");

        // Redis 에 새 RT 가 저장되었는지 확인
        verify(rtBucket).set(eq("refresh-token-new"), any(Duration.class));
    }

    @DisplayName("로그인 성공 – 리프레시 토큰이 없던 기존 회원 → 새 RT 발급·저장")
    @Test
    void login_existingMember_withoutRefreshToken() {
        // given
        // (1) OAuth 로그인 요청/응답
        KakaoLoginParams params = new KakaoLoginParams();
        ReflectionTestUtils.setField(params, "authorizationCode", "code456");

        KakaoInfoResponse kakaoInfo = new KakaoInfoResponse();
        KakaoAccount account = new KakaoAccount();
        ReflectionTestUtils.setField(account, "email", "exist@kakao.com");
        ReflectionTestUtils.setField(kakaoInfo, "id", 654L);
        ReflectionTestUtils.setField(kakaoInfo, "kakaoAccount", account);
        ReflectionTestUtils.setField(authService, "refreshTokenTTL", 600_000L);

        when(requestOAuthInfoService.request(params, "http://localhost:5173"))
            .thenReturn(kakaoInfo);

        // (2) 기존 회원 & RT 미보유
        Member member = createMember("existnick", "exist@kakao.com", 654L);
        ReflectionTestUtils.setField(member, "id", 20L);

        when(memberQueryService.findMemberByProviderId(654L))
            .thenReturn(Optional.of(member));

        // (3) 토큰 생성 stub
        when(jwtUtil.createToken(20L, TokenType.ACCESS_TOKEN)).thenReturn("access-exist");
        when(jwtUtil.createToken(20L, TokenType.REFRESH_TOKEN)).thenReturn("refresh-exist");

        // (4) Redis 버킷 mock
        @SuppressWarnings("unchecked")
        RBucket<String> rtBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(RT_KEY + member.getId())).thenReturn(rtBucket);

        // when
        AuthTokens result = authService.login(params, "http://localhost:5173");

        // then
        assertThat(result.accessToken()).isEqualTo("access-exist");
        assertThat(result.refreshToken()).isEqualTo("refresh-exist");

        // Redis에 RT 가 저장되었는지 확인
        verify(rtBucket).set(eq("refresh-exist"), any(Duration.class));
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

    @DisplayName("로그아웃 성공 RT 삭제 & AT 블랙리스트 등록")
    @Test
    void logout_success() {
        // given
        long memberId = 1L;
        Member member = createMember("nickname", "email@test.com", memberId);
        ReflectionTestUtils.setField(member, "id", memberId);

        String bearerAt = "Bearer some-access-token";
        String accessToken = "some-access-token";
        long remainMs = 120_000L;

        TokenClaimInfo claim = new TokenClaimInfo(memberId);

        // ① JWT 유틸 mock
        when(jwtUtil.resolveToken(bearerAt)).thenReturn(accessToken);
        when(jwtUtil.getTokenClaims(accessToken)).thenReturn(claim);
        when(jwtUtil.getRemainingMillis(accessToken)).thenReturn(remainMs);

        // ② 멤버 조회 mock
        when(memberQueryService.getReferenceById(memberId)).thenReturn(member);

        // ③ Redis 버킷 mock
        RBucket<String> rtBucket = mock(RBucket.class);
        RBucket<String> blBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(RT_KEY + memberId)).thenReturn(rtBucket);
        when(redissonClient.<String>getBucket(BL_KEY + accessToken)).thenReturn(blBucket);

        // when
        assertDoesNotThrow(() -> authService.logout(bearerAt));

        // then
        verify(rtBucket).delete();
        verify(blBucket).set(eq(""), argThat(d -> d.toMillis() == remainMs));
        verifyNoMoreInteractions(rtBucket, blBucket, jwtUtil, memberQueryService);
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

    @DisplayName("RT - 재발급 성공 – Redis 저장소 버전")
    @Test
    void reissue_success() {
        // given
        long memberId = 1L;

        Member member = createMember("nickname", "email@test.com", memberId);
        ReflectionTestUtils.setField(member, "id", memberId);

        String oldRt = "old-refresh-token";
        String newAt = "new-access-token";
        String newRt = "new-refresh-token";
        RBucket<String> bucket = mock(RBucket.class);

        when(jwtUtil.getTokenClaims(oldRt)).thenReturn(new TokenClaimInfo(memberId));
        when(memberQueryService.getReferenceById(memberId)).thenReturn(member);
        when(bucket.get()).thenReturn(oldRt);
        doNothing().when(bucket).set(eq(newRt), any(Duration.class));
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(tokenGenerator.generateToken(memberId)).thenReturn(new AuthTokens(newAt, newRt));

        // refreshTokenTTL 주입 (예: 7일)
        ReflectionTestUtils.setField(authService, "refreshTokenTTL", RT_TTL);

        // when
        AuthTokens result = authService.reissue(oldRt);

        // then
        assertThat(result.accessToken()).isEqualTo(newAt);
        assertThat(result.refreshToken()).isEqualTo(newRt);

        verify(bucket).set(eq(newRt), any(Duration.class));
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

    @DisplayName("재발급 실패 - 저장된 리프레시 토큰이 없음")
    @Test
    void reissue_shouldThrow_whenNoStoredRefreshToken() {
        // given
        Member member = createMember("nick", "email@test.com", 1234L);
        ReflectionTestUtils.setField(member, "id", 1L);

        String rtInCookie = "refresh-token";
        TokenClaimInfo claimInfo = new TokenClaimInfo(member.getId());

        RBucket<String> bucket = mock(RBucket.class);

        when(jwtUtil.getTokenClaims(rtInCookie)).thenReturn(claimInfo);
        when(memberQueryService.getReferenceById(member.getId())).thenReturn(member);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.reissue(rtInCookie))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("customResponseStatus",
                CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND);

        // Redis에 새로운 RT를 저장하려 시도하지 않은 것도 확인
        verify(bucket, never()).set(anyString(), any(Duration.class));
    }
}
