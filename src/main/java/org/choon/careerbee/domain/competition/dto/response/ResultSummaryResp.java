package org.choon.careerbee.domain.competition.dto.response;

public record ResultSummaryResp(
    Long memberId,
    Long solvedSum,
    Long timeSum,
    Long participationDays
) {

    public Double correctRate() {
        return participationDays == 0
            ? 0
            : (double) solvedSum / (participationDays * 4);
    }
}
