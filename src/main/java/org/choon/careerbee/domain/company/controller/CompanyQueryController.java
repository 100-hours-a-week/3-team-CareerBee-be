package org.choon.careerbee.domain.company.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponseEntity;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.service.CompanyQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/companies")
public class CompanyQueryController {

    private final CompanyQueryService queryService;

    @GetMapping
    public ResponseEntity<CommonResponse<CompanyRangeSearchResp>> fetchCompaniesByDistance(
        @ModelAttribute CompanyQueryAddressInfo companyQueryAddressInfo,
        @ModelAttribute CompanyQueryCond companyQueryCond
    ) {
        CompanyRangeSearchResp response = queryService.fetchCompaniesByDistance(
            companyQueryAddressInfo, companyQueryCond);

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 조회에 성공하였습니다."
        );
    }

    @GetMapping("/{companyId}/summary")
    public ResponseEntity<CommonResponse<CompanySummaryInfo>> fetchCompaniesSummary(
        @PathVariable Long companyId
    ) {
        CompanySummaryInfo response = queryService.fetchCompanySummary(companyId);

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 간단 정보 조회에 성공하였습니다."
        );
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CompanyDetailResp>> fetchCompanyDetail(
        @PathVariable Long companyId
    ) {
        CompanyDetailResp response = queryService.fetchCompanyDetail(companyId);

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 상세 정보 조회에 성공하였습니다."
        );
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<CompanySearchResp>> fetchCompanyDetail(
        @RequestParam(value = "keyword") String keyword
    ) {
        CompanySearchResp response = queryService.fetchMatchingCompaniesByKeyword(keyword);

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "매칭 데이터 조회에 성공하였습니다."
        );
    }

    @GetMapping("/{companyId}/locations")
    public ResponseEntity<CommonResponse<CompanyMarkerInfo>> fetchCompanyLocationInfo(
        @PathVariable Long companyId
    ) {
        CompanyMarkerInfo response = queryService.fetchCompanyLocation(companyId);

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 위치 정보 조회에 성공하였습니다."
        );
    }
}
