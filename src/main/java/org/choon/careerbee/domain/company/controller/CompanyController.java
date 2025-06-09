package org.choon.careerbee.domain.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
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
public class CompanyController {

    private final CompanyQueryService queryService;

    @Operation(
        summary = "기업 거리 기반 조회",
        description = "해당 주소 정보와 조건에 맞는 기업을 거리순으로 조회합니다.",
        tags = {"Company"}
    )
    @GetMapping
    public ResponseEntity<CommonResponse<CompanyRangeSearchResp>> fetchCompaniesByDistance(
        @Parameter(description = "조회 기준이 되는 주소 정보 (위도, 경도, 반경 등)")
        @ModelAttribute CompanyQueryAddressInfo companyQueryAddressInfo,

        @Parameter(description = "기업 필터링 조건 (조회 반경)")
        @ModelAttribute CompanyQueryCond companyQueryCond
    ) {
        CompanyRangeSearchResp response = queryService.fetchCompaniesByDistance(
            companyQueryAddressInfo, companyQueryCond);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 조회에 성공하였습니다."
        );
    }

    @Operation(
        summary = "기업 간단 정보 조회",
        description = "기업 ID에 따른 요약 정보를 조회합니다.",
        tags = {"Company"}
    )
    @GetMapping("/{companyId}/summary")
    public ResponseEntity<CommonResponse<CompanySummaryInfo>> fetchCompaniesSummary(
        @PathVariable("companyId") Long companyId
    ) {
        CompanySummaryInfo response = queryService.fetchCompanySummary(companyId);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 간단 정보 조회에 성공하였습니다."
        );
    }

    @Operation(
        summary = "기업 상세 정보 조회",
        description = "기업 ID에 따른 상세 정보를 조회합니다.",
        tags = {"Company"}
    )
    @GetMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CompanyDetailResp>> fetchCompanyDetail(
        @Parameter(description = "기업 ID", example = "1")
        @PathVariable("companyId") Long companyId
    ) {
        CompanyDetailResp response = queryService.fetchCompanyDetail(companyId);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 상세 정보 조회에 성공하였습니다."
        );
    }

    @Operation(
        summary = "기업 검색",
        description = "키워드를 바탕으로 기업을 검색합니다.",
        tags = {"Company"}
    )
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<CompanySearchResp>> searchCompanyByKeyword(
        @Parameter(description = "검색 키워드", example = "네이버")
        @RequestParam(value = "keyword") String keyword
    ) {
        CompanySearchResp response = queryService.fetchMatchingCompaniesByKeyword(keyword);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "매칭 데이터 조회에 성공하였습니다."
        );
    }

    @Operation(
        summary = "특정 기업 위치 정보 조회",
        description = "특정 기업의 위치 정보를 조회합니다.",
        tags = {"Company"}
    )
    @GetMapping("/{companyId}/locations")
    public ResponseEntity<CommonResponse<CompanyMarkerInfo>> fetchCompanyLocationInfo(
        @Parameter(description = "기업 ID", example = "1")
        @PathVariable("companyId") Long companyId
    ) {
        CompanyMarkerInfo response = queryService.fetchCompanyLocation(companyId);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "기업 위치 정보 조회에 성공하였습니다."
        );
    }
}
