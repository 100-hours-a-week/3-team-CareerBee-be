package org.choon.careerbee.domain.competition.controller;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.service.CompetitionCommandService;
import org.choon.careerbee.domain.competition.service.CompetitionQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class CompetitionController {

    private final CompetitionQueryService queryService;
    private final CompetitionCommandService commandService;

    @PostMapping("competitions/{competitionId}")
    public ResponseEntity<CommonResponse<Void>> joinCompetition(
        @PathVariable Long competitionId,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.joinCompetition(competitionId, principalDetails.getId());

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "대회 입장에 성공하였습니다."
        );
    }

    @PostMapping("competitions/{competitionId}/results")
    public ResponseEntity<CommonResponse<Void>> submitCompetitionResult(
        @PathVariable Long competitionId,
        @RequestBody CompetitionResultSubmitReq submitReq,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.submitCompetitionResult(competitionId, submitReq, principalDetails.getId());

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "대회 제출에 성공하였습니다."
        );
    }

    @GetMapping("members/competitions/{competitionId}")
    public ResponseEntity<CommonResponse<CompetitionParticipationResp>> checkCompetitionParticipation(
        @PathVariable Long competitionId,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        CompetitionParticipationResp response = queryService.checkCompetitionParticipationById(
            competitionId, principalDetails.getId()
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "대회 참여 여부 조회에 성공하였습니다."
        );
    }

    @GetMapping("competitions/{competitionId}/problems")
    public ResponseEntity<CommonResponse<CompetitionProblemResp>> fetchCompetitionProblems(
        @PathVariable Long competitionId
    ) {
        CompetitionProblemResp response = queryService.fetchProblems(competitionId);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "대회 문제 조회에 성공하였습니다."
        );
    }
}
