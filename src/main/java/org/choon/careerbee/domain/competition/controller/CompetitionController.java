package org.choon.careerbee.domain.competition.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.response.CompetitionGradingResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.service.command.CompetitionCommandService;
import org.choon.careerbee.domain.competition.service.query.CompetitionQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class CompetitionController {

    private final CompetitionQueryService queryService;
    private final CompetitionCommandService commandService;

    @Value("${careerbee.allow-date-param:false}")
    private boolean allowDateParam;

    @PostMapping("competitions/{competitionId}")
    public ResponseEntity<CommonResponse<Void>> joinCompetition(
        @PathVariable("competitionId") Long competitionId,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.joinCompetition(competitionId, principalDetails.getId());

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "대회 입장에 성공하였습니다."
        );
    }

    @PostMapping("competitions/{competitionId}/results")
    public ResponseEntity<CommonResponse<CompetitionGradingResp>> submitCompetitionResult(
        @PathVariable("competitionId") Long competitionId,
        @RequestBody CompetitionResultSubmitReq submitReq,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        CompetitionGradingResp response = commandService.submitCompetitionResult(competitionId,
            submitReq, principalDetails.getId());

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "대회 제출에 성공하였습니다."
        );
    }

    @GetMapping("members/competitions/{competitionId}")
    public ResponseEntity<CommonResponse<CompetitionParticipationResp>> checkCompetitionParticipation(
        @PathVariable("competitionId") Long competitionId,
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
        @PathVariable("competitionId") Long competitionId
    ) {
        CompetitionProblemResp response = queryService.fetchProblems(competitionId);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "대회 문제 조회에 성공하였습니다."
        );
    }

    @GetMapping("competitions/rankings")
    public ResponseEntity<CommonResponse<CompetitionRankingResp>> fetchCompetitionRankings(
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate todayDate
    ) {
        LocalDate today = allowDateParam && todayDate != null
            ? todayDate
            : LocalDate.now();

        CompetitionRankingResp response = queryService.fetchRankings(today);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "랭킹조회에 성공하였습니다."
        );
    }

    @GetMapping("members/competitions/rankings")
    public ResponseEntity<CommonResponse<MemberRankingResp>> fetchMemberCompetitionRanking(
        @AuthenticationPrincipal PrincipalDetails principalDetails,

        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate todayDate
    ) {
        LocalDate today = allowDateParam && todayDate != null
            ? todayDate
            : LocalDate.now();

        MemberRankingResp response = queryService.fetchMemberCompetitionRankingById(
            principalDetails.getId(), today
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "내 랭킹 조회에 성공하였습니다."
        );
    }

    @GetMapping("competitions/ids")
    public ResponseEntity<CommonResponse<CompetitionIdResp>> fetchTodayCompetitionId(
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate todayDate
    ) {
        LocalDate today = allowDateParam && todayDate != null
            ? todayDate
            : LocalDate.now();
        CompetitionIdResp response = queryService.fetchCompetitionIdBy(today);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "오늘 대회 id 조회에 성공하였습니다."
        );
    }

    @GetMapping("members/competitions/rankings/live")
    public ResponseEntity<CommonResponse<MemberLiveRankingResp>> fetchMemberLiveRanking(
        @AuthenticationPrincipal PrincipalDetails principalDetails,

        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate todayDate
    ) {
        LocalDate today = allowDateParam && todayDate != null
            ? todayDate
            : LocalDate.now();

        MemberLiveRankingResp response = queryService.fetchMemberLiveRanking(
            principalDetails.getId(), today
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "실시간 내 랭킹 조회에 성공하였습니다."
        );
    }

    @GetMapping("competitions/rankings/live")
    public ResponseEntity<CommonResponse<LiveRankingResp>> fetchLiveRanking(
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate todayDate
    ) {
        LocalDate today = allowDateParam && todayDate != null
            ? todayDate
            : LocalDate.now();

        LiveRankingResp response = queryService.fetchLiveRanking(today);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "실시간 랭킹 조회에 성공하였습니다."
        );
    }

}
