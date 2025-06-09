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
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.UploadCompleteReq;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.dto.response.ResumeDraftResp;
import org.choon.careerbee.domain.member.service.MemberCommandService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PatchMapping("/resume")
    public ResponseEntity<CommonResponse<Void>> updateResumeInfo(
        @RequestBody UpdateResumeReq updateResumeReq,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.updateResumeInfo(updateResumeReq, principalDetails.getId());

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "이력 정보 수정이 완료되었습니다."
        );
    }

    @PostMapping("/resume")
    public ResponseEntity<CommonResponse<ResumeDraftResp>> generateResumeDraft(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        ResumeDraftResp response = commandService.generateResumeDraft(principalDetails.getId());

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "이력서 초안 생성에 성공하였습니다."
        );
    }

    @PostMapping("resume/complete-upload")
    public ResponseEntity<CommonResponse<ExtractResumeResp>> extractResumeInfo(
        @RequestBody UploadCompleteReq uploadCompleteReq
    ) {
        ExtractResumeResp response = commandService.extractResumeInfoFromAi(uploadCompleteReq);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "이력서 정보 추출에 성공하였습니다."
        );
    }
}
