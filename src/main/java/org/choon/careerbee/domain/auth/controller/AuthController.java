package org.choon.careerbee.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    log.info("oauthProvider : {}", type);
    OAuthLoginUrlResp response = authService.getOAuthLoginUrl(type);

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS.withMessage("소셜 로그인 url 조회에 성공하였습니다."))
    );
  }
}
