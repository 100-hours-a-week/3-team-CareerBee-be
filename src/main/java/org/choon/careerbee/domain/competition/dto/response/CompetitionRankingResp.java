package org.choon.careerbee.domain.competition.dto.response;

import java.util.List;

public record CompetitionRankingResp(
    List<RankingInfo> daily,
    List<RankingInfoWithContinuous> week,
    List<RankingInfoWithContinuous> month
) {

    public record RankingInfo(
        String nickname,
        String badgeUrl,
        String profileUrl,
        long elapsedTime,
        short solvedCount
    ) {

    }

    public record RankingInfoWithContinuous(
        String nickname,
        String badgeUrl,
        String profileUrl,
        int continuous,
        short solvedCount
    ) {

    }
}
