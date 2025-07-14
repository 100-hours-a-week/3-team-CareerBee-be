package org.choon.careerbee.domain.interview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.request.SubmitAnswerReq;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemDetailResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.service.command.InterviewCommandService;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            "비회원 면접문제 조회에 성공하였습니다."
        );
    }

    @GetMapping("/members/interview-problems")
    public ResponseEntity<CommonResponse<InterviewProblemDetailResp>> fetchMemberInterviewProblem(
        @RequestParam(name = "type") ProblemType type,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        InterviewProblemDetailResp response = queryService.fetchMemberInterviewProblemByType(
            type, principalDetails.getId()
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "회원 면접문제 조회에 성공하였습니다."
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

    @PatchMapping("members/interview-problems/{problemId}")
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

    @PatchMapping("members/interview-problems/{problemId}/cancel")
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

    @GetMapping("members/interview-problems/saved")
    public ResponseEntity<CommonResponse<SaveInterviewProblemResp>> fetchSaveInterviewProblem(
        @RequestParam(name = "cursor", required = false) Long cursor,
        @RequestParam(name = "size", defaultValue = "5") int size,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        SaveInterviewProblemResp response = queryService.fetchSaveInterviewProblem(
            principalDetails.getId(),
            cursor,
            size
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "저장된 면접문제 조회에 성공하였습니다."
        );
    }

    @PostMapping("interview-problems/answers")
    public ResponseEntity<CommonResponse<AiFeedbackResp>> submitAnswer(
        @RequestBody SubmitAnswerReq submitAnswerReq,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        AiFeedbackResp response = commandService.submitAnswer(
            submitAnswerReq,
            principalDetails.getId()
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "문제 답변에 대한 피드백입니다."
        );
    }

    @PatchMapping("members/interview-problems/next")
    public ResponseEntity<CommonResponse<Void>> requestNextProblem(
        @RequestParam(name = "type") ProblemType type,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.requestNextProblem(
            type, principalDetails.getId()
        );

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "다음 문제 풀이 요청이 성공하였습니다."
        );
    }
}
