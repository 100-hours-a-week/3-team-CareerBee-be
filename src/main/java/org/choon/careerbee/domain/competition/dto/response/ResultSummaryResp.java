package org.choon.careerbee.domain.competition.dto.response;

public record ResultSummaryResp(
    Long memberId,
    Long solvedSum,
    Long timeSum,
    Long participationDays
) {

    private static final int PROBLEM_CHOICE_COUNT = 5;

    public Double correctRate() {
        return participationDays == 0
            ? 0
            : (double) solvedSum / (participationDays * PROBLEM_CHOICE_COUNT);
    }
}
