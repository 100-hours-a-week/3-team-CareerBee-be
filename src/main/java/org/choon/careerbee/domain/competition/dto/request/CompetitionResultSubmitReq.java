package org.choon.careerbee.domain.competition.dto.request;

public record CompetitionResultSubmitReq(
    Short solvedCount,
    Integer elapsedTime
) {

}
