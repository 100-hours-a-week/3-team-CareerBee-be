package org.choon.careerbee.domain.competition.dto.response;

import java.util.List;

public record CompetitionRankingResp(
    List<RankingInfo> daily,
    List<RankingInfoWithContinuousAndCorrectRate> week,
    List<RankingInfoWithContinuousAndCorrectRate> month
) {

    public record RankingInfo(
        String nickname,
        String badgeUrl,
        String profileUrl,
        long elapsedTime,
        short solvedCount
    ) {

    }

    public record RankingInfoWithContinuousAndCorrectRate(
        String nickname,
        String badgeUrl,
        String profileUrl,
        int continuous,
        double correctRate
    ) {

    }
}
