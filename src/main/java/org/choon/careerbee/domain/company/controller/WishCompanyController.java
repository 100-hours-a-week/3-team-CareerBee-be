package org.choon.careerbee.domain.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.company.dto.response.CheckWishCompanyResp;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.service.CompanyCommandService;
import org.choon.careerbee.domain.company.service.CompanyQueryService;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/members/wish-companies")
public class WishCompanyController {

    private final CompanyCommandService commandService;
    private final CompanyQueryService queryService;

    @Operation(
        summary = "관심 기업 등록",
        description = "회원이 특정 기업을 관심 기업으로 등록합니다.",
        tags = {"WishCompany"},
        security = {@SecurityRequirement(name = "JWT")}
    )
    @PostMapping("/{companyId}")
    public ResponseEntity<CommonResponse<Void>> registWishCompany(
        @Parameter(description = "관심 등록할 기업 ID", example = "1")
        @PathVariable("companyId") Long companyId,

        @Parameter(hidden = true)
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.registWishCompany(principalDetails.getId(), companyId);

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "관심기업 등록에 성공하였습니다."
        );
    }

    @Operation(
        summary = "관심 기업 삭제",
        description = "회원이 등록한 관심 기업을 삭제합니다.",
        tags = {"WishCompany"},
        security = {@SecurityRequirement(name = "JWT")}
    )
    @DeleteMapping("/{companyId}")
    public ResponseEntity<CommonResponse<Void>> deleteWishCompany(
        @Parameter(description = "삭제할 관심 기업 ID", example = "1")
        @PathVariable("companyId") Long companyId,

        @Parameter(hidden = true)
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.deleteWishCompany(principalDetails.getId(), companyId);

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "관심기업 삭제에 성공하였습니다."
        );
    }

    @Operation(
        summary = "관심 기업 여부 확인",
        description = "회원이 특정 기업을 관심 기업으로 등록했는지 확인합니다.",
        tags = {"WishCompany"},
        security = {@SecurityRequirement(name = "JWT")}
    )
    @GetMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CheckWishCompanyResp>> checkWishCompany(
        @Parameter(description = "확인할 기업 ID", example = "1")
        @PathVariable("companyId") Long companyId,

        @Parameter(hidden = true)
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        CheckWishCompanyResp response = queryService.checkWishCompany(principalDetails.getId(),
            companyId);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "관심기업 여부 조회에 성공하였습니다."
        );
    }

    @Operation(
        summary = "관심 기업 ID 목록 조회",
        description = "회원이 등록한 모든 관심 기업의 ID 목록을 조회합니다.",
        tags = {"WishCompany"},
        security = {@SecurityRequirement(name = "JWT")}
    )
    @GetMapping("/id-list")
    public ResponseEntity<CommonResponse<WishCompanyIdResp>> fetchCompanyDetail(
        @Parameter(hidden = true)
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        WishCompanyIdResp response = queryService.fetchWishCompanyIds(principalDetails.getId());

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "관심 기업 아이디 조회에 성공하였습니다."
        );
    }

    @GetMapping()
    public ResponseEntity<CommonResponse<WishCompaniesResp>> fetchWishCompanies(
        @RequestParam(name = "cursor", required = false) Long cursor,
        @RequestParam(name = "size", defaultValue = "5") int size,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        WishCompaniesResp response = queryService.fetchWishCompanies(
            principalDetails.getId(),
            cursor,
            size
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "관심 기업 목록 조회에 성공하였습니다."
        );
    }

}
