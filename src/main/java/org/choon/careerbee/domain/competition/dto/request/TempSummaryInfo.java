package org.choon.careerbee.domain.competition.dto.request;

public record TempSummaryInfo(
    Long memberId,
    Short solvedSum,
    Long timeSum,
    Double correctRate,
    Integer maxStreak
) {

}
