package org.choon.careerbee.domain.interview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.service.command.InterviewCommandService;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/")
public class InterviewController {

    private final InterviewQueryService queryService;
    private final InterviewCommandService commandService;

    @GetMapping("interview-problems")
    public ResponseEntity<CommonResponse<InterviewProblemResp>> fetchInterviewProblem() {
        InterviewProblemResp response = queryService.fetchInterviewProblem();

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "면접문제 조회에 성공하였습니다."
        );
    }

    @GetMapping("members/interview-problems/{problemId}")
    public ResponseEntity<CommonResponse<CheckProblemSolveResp>> checkInterviewProblemSolved(
        @PathVariable Long problemId,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        CheckProblemSolveResp response = queryService.checkInterviewProblemSolved(
            problemId, principalDetails.getId()
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "면접문제 풀이 여부 조회에 성공하였습니다."
        );
    }

    @PostMapping("members/interview-problems/{problemId}")
    public ResponseEntity<CommonResponse<Void>> saveInterviewProblem(
        @PathVariable Long problemId,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.saveInterviewProblem(problemId, principalDetails.getId());

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "면접문제 저장에 성공하였습니다."
        );
    }

    @PatchMapping("members/interview-problems/{problemId}")
    public ResponseEntity<CommonResponse<Void>> cancelSaveInterviewProblem(
        @PathVariable Long problemId,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.cancelSaveInterviewProblem(problemId, principalDetails.getId());

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "면접문제 저장 취소에 성공하였습니다."
        );
    }
}
