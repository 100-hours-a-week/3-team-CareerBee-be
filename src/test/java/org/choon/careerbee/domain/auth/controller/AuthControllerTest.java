package org.choon.careerbee.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.TokenFixture.createToken;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.auth.repository.TokenRepository;
import org.choon.careerbee.domain.auth.service.oauth.OAuthApiClient;
import org.choon.careerbee.domain.auth.service.oauth.RequestOAuthInfoService;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoInfoResponse;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoInfoResponse.Profile;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoLoginParams;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.member.service.MemberCommandService;
import org.choon.careerbee.filter.jwt.JwtAuthenticationFilter;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberCommandService memberCommandService;

    @Autowired
    private OAuthApiClient kakaoApiClient;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestOAuthInfoService requestOAuthInfoService;

    private String testAccessToken;
    private Member testMember;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter(new JwtAuthenticationFilter(jwtUtil, tokenRepository, objectMapper))
            .apply(springSecurity())
            .build();

        testMember = memberRepository.saveAndFlush(createMember("testnick", "test@test.com", 1L));

        testAccessToken =
            "Bearer " + jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("소셜 로그인 URL 요청이 성공적으로 처리된다")
    void getOAuthLoginUrl_success() throws Exception {
        // given
        String origin = "http://localhost:5173";
        String expectedLoginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=1342353523423&redirect_uri=https://test.co.kr";

        // when & then
        mockMvc.perform(get("/api/v1/auth/oauth")
                .param("type", "kakao")
                .header("Origin", origin)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("소셜 로그인 url 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.data.loginUrl").value(expectedLoginUrl));
    }

    @Test
    @DisplayName("소셜 로그인 URL 요청 시 Origin 헤더 누락 시 400 예외 발생")
    void getOAuthLoginUrl_shouldReturn400_whenOriginHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth")
                .param("type", "kakao")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.INVALID_INPUT_VALUE.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.INVALID_INPUT_VALUE.getHttpStatusCode()));
    }

    @Test
    @DisplayName("카카오 로그인 요청 성공 - 신규 회원인 경우")
    void kakaoLogin_success_newMember() throws Exception {
        // given
        KakaoLoginParams loginParams = new KakaoLoginParams();
        ReflectionTestUtils.setField(loginParams, "authorizationCode", "test-auth-code");

        KakaoInfoResponse mockOAuthInfo = new KakaoInfoResponse();
        KakaoInfoResponse.KakaoAccount kakaoAccount = new KakaoInfoResponse.KakaoAccount();
        Profile profile = new Profile();
        ReflectionTestUtils.setField(profile, "nickname", "testNick");
        ReflectionTestUtils.setField(kakaoAccount, "email", "mock@kakao.com");
        ReflectionTestUtils.setField(kakaoAccount, "profile", profile);
        ReflectionTestUtils.setField(mockOAuthInfo, "kakaoAccount", kakaoAccount);
        ReflectionTestUtils.setField(mockOAuthInfo, "id", 12345L);

        when(requestOAuthInfoService.request(any(), any())).thenReturn(mockOAuthInfo);

        long countBefore = memberRepository.count();

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/tokens/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:5173")
                .content(objectMapper.writeValueAsString(loginParams)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("로그인에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.data.accessToken").exists());

        long countAfter = memberRepository.count();
        assertThat(countBefore + 1).isEqualTo(countAfter);
    }

    @Test
    @DisplayName("카카오 로그인 요청 성공 - 기존 회원인 경우")
    void kakaoLogin_success_existingMember() throws Exception {
        // given
        KakaoLoginParams loginParams = new KakaoLoginParams();
        ReflectionTestUtils.setField(loginParams, "authorizationCode", "test-auth-code");

        KakaoInfoResponse mockOAuthInfo = new KakaoInfoResponse();
        KakaoInfoResponse.KakaoAccount kakaoAccount = new KakaoInfoResponse.KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "mock@kakao.com");
        ReflectionTestUtils.setField(mockOAuthInfo, "kakaoAccount", kakaoAccount);
        ReflectionTestUtils.setField(mockOAuthInfo, "id", 12345L);

        memberRepository.saveAndFlush(createMember("testnick", "mock@kakao.com", 12345L));
        when(requestOAuthInfoService.request(any(), any())).thenReturn(mockOAuthInfo);

        long countBefore = memberRepository.count();

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/tokens/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:5173")
                .content(objectMapper.writeValueAsString(loginParams)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("로그인에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.data.accessToken").exists());

        long countAfter = memberRepository.count();
        assertThat(countBefore).isEqualTo(countAfter);
    }

    @Test
    @DisplayName("카카오 로그인 요청 실패 - 이미 탈퇴한 회원인 경우 410 예외 발생")
    void kakaoLogin_fail_withdrawnMember() throws Exception {
        // given
        KakaoLoginParams loginParams = new KakaoLoginParams();
        ReflectionTestUtils.setField(loginParams, "authorizationCode", "test-auth-code");

        KakaoInfoResponse mockOAuthInfo = new KakaoInfoResponse();
        KakaoInfoResponse.KakaoAccount kakaoAccount = new KakaoInfoResponse.KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "mock@kakao.com");
        ReflectionTestUtils.setField(mockOAuthInfo, "kakaoAccount", kakaoAccount);
        ReflectionTestUtils.setField(mockOAuthInfo, "id", 12345L);

        Member member = memberRepository.saveAndFlush(
            createMember("testnick", "mock@kakao.com", 12345L));
        ReflectionTestUtils.setField(member, "withdrawnAt", LocalDateTime.now());
        when(requestOAuthInfoService.request(any(), any())).thenReturn(mockOAuthInfo);

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/tokens/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:5173")
                .content(objectMapper.writeValueAsString(loginParams)))
            .andExpect(status().isGone())
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.WITHDRAWAL_MEMBER.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(410))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("로그아웃 요청이 성공적으로 처리된다")
    void logout_success() throws Exception {
        // given
        String testRefreshTokenValue = jwtUtil.createToken(testMember.getId(),
            TokenType.REFRESH_TOKEN);
        tokenRepository.saveAndFlush(
            createToken(testMember, testRefreshTokenValue, TokenStatus.LIVE)
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", testAccessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그아웃 요청 실패 - Authorization 헤더 누락")
    void logout_shouldReturn400_whenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(
                CustomResponseStatus.NULL_JWT.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.NULL_JWT.getHttpStatusCode()));
    }

    @Test
    @DisplayName("토큰 재발급 요청이 성공적으로 처리된다")
    void reissue_success() throws Exception {
        // given
        String validRefreshToken = jwtUtil.createToken(testMember.getId(), TokenType.REFRESH_TOKEN);
        tokenRepository.saveAndFlush(createToken(testMember, validRefreshToken, TokenStatus.LIVE));

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")
                .cookie(new Cookie("refreshToken", validRefreshToken))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("토큰 재발급에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.data.newAccessToken").isNotEmpty())
            .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 누락")
    void reissue_shouldReturn400_whenCookieMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reissue")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.INVALID_INPUT_VALUE.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.INVALID_INPUT_VALUE.getHttpStatusCode()));
    }
}
