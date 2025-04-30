package org.choon.careerbee.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.dto.response.LoginResp;
import org.choon.careerbee.domain.auth.dto.response.ReissueResp;
import org.choon.careerbee.domain.auth.dto.response.TokenAndUserInfo;
import org.choon.careerbee.domain.auth.service.auth.AuthService;
import org.choon.careerbee.domain.auth.service.oauth.kakao.KakaoLoginParams;
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
public class AuthController {
  private final AuthService authService;

  @GetMapping("/oauth")
  public ResponseEntity<ApiResponse<OAuthLoginUrlResp>> getOAuthLoginUrl(
      @RequestParam(value = "type") String type
  ) {
    OAuthLoginUrlResp response = authService.getOAuthLoginUrl(type);

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS.withMessage("소셜 로그인 url 조회에 성공하였습니다."))
    );
  }

  @PostMapping("/oauth/tokens/kakao")
  public ResponseEntity<ApiResponse<LoginResp>> kakaoLogin(
      @RequestBody KakaoLoginParams kakaoParams,
      HttpServletResponse response
  ) {
    TokenAndUserInfo tokenAndUserInfo = authService.login(kakaoParams);
    setTokenInCookie(response, tokenAndUserInfo.authTokens());

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        new LoginResp(tokenAndUserInfo.authTokens().accessToken(), tokenAndUserInfo.userInfo()),
        CustomResponseStatus.SUCCESS.withMessage("로그인에 성공하였습니다."))
    );
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader("Authorization") String accessToken
  ) {
    authService.logout(accessToken);

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("로그아웃에 성공하였습니다."))
    );
  }

  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<ReissueResp>> reissue(
      @CookieValue("refreshToken") String refreshToken,
      HttpServletResponse response
  ) {
    AuthTokens authTokens = authService.reissue(refreshToken);
    setTokenInCookie(response, authTokens);

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        new ReissueResp(authTokens.accessToken()),
        CustomResponseStatus.SUCCESS.withMessage("토큰 재발급에 성공하였습니다."))
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
//    cookie.setDomain(cookieDomain);
    return cookie;
  }
}
