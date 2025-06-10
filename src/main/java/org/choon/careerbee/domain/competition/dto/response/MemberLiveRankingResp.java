package org.choon.careerbee.domain.competition.dto.response;

public record MemberLiveRankingResp(
    Long rank,
    Integer elapsedTime,
    short solvedCount
) {

}
