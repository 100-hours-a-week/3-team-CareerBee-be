package org.choon.careerbee.domain.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.ApiResponse;
import org.choon.careerbee.common.dto.ApiResponseEntity;
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

    @GetMapping()
    public ResponseEntity<ApiResponse<MyInfoResp>> fetchMyInfo(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        MyInfoResp response = queryService.getMyInfoByMemberId(principalDetails.getId());

        return ApiResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "유저 정보 조회에 성공하였습니다."
        );
    }
}
