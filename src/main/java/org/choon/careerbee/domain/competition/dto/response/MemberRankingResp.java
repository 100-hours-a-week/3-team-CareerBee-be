package org.choon.careerbee.domain.competition.dto.response;

public record MemberRankingResp(
    MemberDayRankInfo daily,
    MemberWeekAndMonthRankInfo week,
    MemberWeekAndMonthRankInfo month
) {

    public record MemberDayRankInfo(
        Long rank,
        Long elapsedTime,
        short solvedCount
    ) {

    }

    public record MemberWeekAndMonthRankInfo(
        Long rank,
        Integer continuous,
        Integer correctRate
    ) {

        public static MemberWeekAndMonthRankInfo from(
            Long rank,
            Integer continuous,
            Double correctRate
        ) {
            return new MemberWeekAndMonthRankInfo(
                rank,
                continuous,
                correctRate != null ? correctRate.intValue() : null
            );
        }
    }
}
