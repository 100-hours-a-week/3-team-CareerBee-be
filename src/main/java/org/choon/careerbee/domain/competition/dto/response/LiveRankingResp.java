package org.choon.careerbee.domain.competition.dto.response;

import java.util.List;

public record LiveRankingResp(
    List<RankerInfo> rankings
) {

    public record RankerInfo(
        Long rank,
        String nickname,
        String profileImgUrl,
        String badgeImgUrl,
        Integer elapsedTime,
        short solvedCount
    ) {

    }
}
