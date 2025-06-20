package org.choon.careerbee.domain.competition.dto.response;

import java.util.List;

public record CompetitionGradingResp(
    List<CompetitionGradingInfo> gradingResults
) {

    public record CompetitionGradingInfo(
        Long problemId,
        Boolean isCorrect,
        String solution
    ) {

    }
}
