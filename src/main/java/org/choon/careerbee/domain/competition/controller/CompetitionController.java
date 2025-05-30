package org.choon.careerbee.domain.competition.controller;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.competition.service.CompetitionCommandService;
import org.choon.careerbee.domain.competition.service.CompetitionQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}
