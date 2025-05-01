package org.choon.careerbee.domain.company.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponse;
import org.choon.careerbee.common.dto.ApiResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.company.service.CompanyCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/members/wish-companies")
public class CompanyCommandController {
  private final CompanyCommandService commandService;

  @PostMapping("/{companyId}")
  public ResponseEntity<ApiResponse<Void>> registWishCompany(
      @PathVariable Long companyId,
      @AuthenticationPrincipal PrincipalDetails principalDetails
  ) {
    commandService.registWishCompany(principalDetails.getId(), companyId);

    return ApiResponseEntity.ok(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("관심기업 등록에 성공하였습니다.")
    );
  }
}
