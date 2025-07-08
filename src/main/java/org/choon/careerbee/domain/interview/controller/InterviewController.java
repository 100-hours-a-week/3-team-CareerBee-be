package org.choon.careerbee.domain.interview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.service.command.InterviewCommandService;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
