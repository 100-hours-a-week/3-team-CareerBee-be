package org.choon.careerbee.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.service.MemberCommandService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberQueryService queryService;
    private final MemberCommandService commandService;

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 회원 정보를 조회합니다.",
        tags = {"Member"},
        security = {@SecurityRequirement(name = "JWT")}
    )
    @GetMapping
    public ResponseEntity<CommonResponse<MyInfoResp>> fetchMyInfo(
        @Parameter(hidden = true)
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        MyInfoResp response = queryService.getMyInfoByMemberId(principalDetails.getId());

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "유저 정보 조회에 성공하였습니다."
        );
    }
}
