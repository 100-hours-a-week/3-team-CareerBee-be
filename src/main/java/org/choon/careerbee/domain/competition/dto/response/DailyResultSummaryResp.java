package org.choon.careerbee.domain.competition.dto.response;

public record DailyResultSummaryResp(
    Long memberId,
    Short solvedSum,
    Integer timeSum
) {

}
