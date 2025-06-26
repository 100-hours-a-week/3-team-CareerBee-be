package org.choon.careerbee.domain.competition.dto.response;

import java.util.List;

public record CompetitionRankingResp(
    List<RankingInfo> daily,
    List<RankingInfoWithContinuousAndCorrectRate> week,
    List<RankingInfoWithContinuousAndCorrectRate> month
) {

    public record RankingInfo(
        String nickname,
        String profileUrl,
        long elapsedTime,
        short solvedCount
    ) {

    }

    public record RankingInfoWithContinuousAndCorrectRate(
        String nickname,
        String profileUrl,
        int continuous,
        Integer correctRate
    ) {

        public static RankingInfoWithContinuousAndCorrectRate from(
            String nickname,
            String imgUrl,
            Integer maxContinuousDays,
            Double correctRate
        ) {
            return new RankingInfoWithContinuousAndCorrectRate(
                nickname,
                imgUrl,
                maxContinuousDays,
                correctRate != null ? correctRate.intValue() : null
            );
        }
    }
}
