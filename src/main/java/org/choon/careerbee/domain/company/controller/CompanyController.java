package org.choon.careerbee.domain.company.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponse;
import org.choon.careerbee.common.dto.ApiResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.service.CompanyCommandService;
import org.choon.careerbee.domain.company.service.CompanyQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/companies")
public class CompanyController {
  private final CompanyQueryService queryService;
  private final CompanyCommandService commandService;

  @GetMapping
  public ResponseEntity<ApiResponse<CompanyRangeSearchResp>> fetchCompaniesByDistance(
      @ModelAttribute CompanyQueryAddressInfo companyQueryAddressInfo,
      @ModelAttribute CompanyQueryCond companyQueryCond
  ) {
    CompanyRangeSearchResp response = queryService.fetchCompaniesByDistance(companyQueryAddressInfo, companyQueryCond);

    return ApiResponseEntity.ok(
        response,
        CustomResponseStatus.SUCCESS.withMessage("기업 조회에 성공하였습니다.")
    );
  }
}
