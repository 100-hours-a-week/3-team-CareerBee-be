package org.choon.careerbee.domain.competition.dto.request;

public record TempSummaryInfo(
    Long memberId,
    Long solvedSum,
    Long timeSum,
    Double correctRate,
    Integer maxStreak
) {

}
