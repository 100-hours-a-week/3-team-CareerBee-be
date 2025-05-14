package org.choon.careerbee.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponseEntity;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.response.LoginResp;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.dto.response.ReissueResp;
import org.choon.careerbee.domain.auth.dto.response.TokenAndUserInfo;
import org.choon.careerbee.domain.auth.service.auth.AuthService;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoLoginParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "소셜 로그인 관련 API")
public class AuthController {

    @Value("${server.servlet.session.cookie.domain}")
    private String cookieDomain;

    private final AuthService authService;

    @Operation(
        summary = "소셜 로그인 URL 요청",
        description = "카카오, 구글 등 소셜 로그인 타입에 맞는 로그인 URL을 반환합니다.",
        tags = {"Auth"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "소셜 로그인 url 조회에 성공하였습니다."),
    })
    @GetMapping("/oauth")
    public ResponseEntity<CommonResponse<OAuthLoginUrlResp>> getOAuthLoginUrl(
        @Parameter(name = "type", description = "소셜 로그인 타입 (ex: KAKAO, GOOGLE)", required = true)
        @RequestParam(value = "type") String type
    ) {
        OAuthLoginUrlResp response = authService.getOAuthLoginUrl(type);

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "소셜 로그인 url 조회에 성공하였습니다."
        );
    }

    @Operation(
        summary = "카카오 로그인 요청",
        description = "카카오 소셜 로그인을 진행합니다.",
        tags = {"Auth"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인에 성공하였습니다."),
    })
    @PostMapping("/oauth/tokens/kakao")
    public ResponseEntity<CommonResponse<LoginResp>> kakaoLogin(
        @Parameter(name = "kakaoParams", description = "KAKAO 로부터 발급받은 Authorization Code", required = true)
        @RequestBody KakaoLoginParams kakaoParams,
        HttpServletResponse response
    ) {
        TokenAndUserInfo tokenAndUserInfo = authService.login(kakaoParams);
        setTokenInCookie(response, tokenAndUserInfo.authTokens());

        return ApiResponseEntity.ok(
            new LoginResp(tokenAndUserInfo.authTokens().accessToken(), tokenAndUserInfo.userInfo()),
            CustomResponseStatus.SUCCESS,
            "로그인에 성공하였습니다."
        );
    }

    @Operation(
        summary = "로그아웃 요청",
        description = "로그아웃을 진행합니다.",
        tags = {"Auth"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "로그아웃에 성공하였습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다."),
        @ApiResponse(responseCode = "404", description = "유효한 리프레시 토큰이 존재하지 않습니다.")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
        @Parameter(description = "AccessToken (Bearer {token})", required = true)
        @RequestHeader("Authorization") String accessToken
    ) {
        authService.logout(accessToken);

        return ApiResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "로그아웃에 성공하였습니다."
        );
    }

    @Operation(
        summary = "토큰 재발급",
        description = """
            쿠키에 저장된 RefreshToken을 통해 AccessToken을 재발급합니다.\n
            ✅ Swagger에서는 HttpOnly 쿠키 전송이 불가능하므로 이 API는 Swagger UI에서 테스트할 수 없습니다.
            Postman 또는 실제 프론트엔드 클라이언트를 통해 테스트해주세요.
            """,
        tags = {"Auth"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다."),
        @ApiResponse(responseCode = "404", description = "유효한 리프레시 토큰이 존재하지 않습니다."),
        @ApiResponse(responseCode = "409", description = "잘못된 리프레시 토큰입니다.")
    })
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<ReissueResp>> reissue(
        @Parameter(description = "HttpOnly 쿠키에 저장된 리프레시 토큰", in = ParameterIn.COOKIE, required = true)
        @CookieValue("refreshToken") String refreshToken,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) {
        AuthTokens authTokens = authService.reissue(refreshToken);
        setTokenInCookie(response, authTokens);

        return ApiResponseEntity.ok(
            new ReissueResp(authTokens.accessToken()),
            CustomResponseStatus.SUCCESS,
            "토큰 재발급에 성공하였습니다."
        );
    }

    private void setTokenInCookie(HttpServletResponse response, AuthTokens tokens) {
        response.addCookie(createCookie(tokens.refreshToken()));
    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refreshToken", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }
}
