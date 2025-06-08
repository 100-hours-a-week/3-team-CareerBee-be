package org.choon.careerbee.domain.competition.repository.custom;

import static org.choon.careerbee.domain.competition.domain.QCompetitionSummary.competitionSummary;
import static org.choon.careerbee.domain.member.entity.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfo;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfoWithContinuousAndCorrectRate;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp.MemberDayRankInfo;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp.MemberWeekAndMonthRankInfo;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CompetitionSummaryCustomRepositoryImpl implements
    CompetitionSummaryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CompetitionRankingResp fetchRankings(LocalDate today) {
        LocalDate dailyEnd  = latestDailyEnd(today);
        LocalDate weekEnd   = currentPeriodEnd(SummaryType.WEEK,  today);
        LocalDate monthEnd  = currentPeriodEnd(SummaryType.MONTH, today);

        List<RankingInfo> daily = queryFactory
            .select(Projections.constructor(
                RankingInfo.class,
                member.nickname,
                member.imgUrl,
                member.imgUrl,
                competitionSummary.elapsedTime,
                competitionSummary.solvedCount))
            .from(competitionSummary)
            .join(competitionSummary.member, member)
            .where(
                competitionSummary.type.eq(SummaryType.DAY),
                competitionSummary.periodEnd.eq(dailyEnd))
            .orderBy(competitionSummary.ranking.asc())
            .limit(10)
            .fetch();

        List<RankingInfoWithContinuousAndCorrectRate> week = fetchRankBySummaryTypeAndPeriodEnd(SummaryType.WEEK, weekEnd);
        List<RankingInfoWithContinuousAndCorrectRate> month = fetchRankBySummaryTypeAndPeriodEnd(SummaryType.MONTH, monthEnd);

        return new CompetitionRankingResp(daily, week, month);
    }

    @Override
    public MemberRankingResp fetchMemberRankingById(Long accessMemberId, LocalDate today) {
        MemberDayRankInfo dailyRanking = fetchDailyRankingByDate(
            accessMemberId, today
        );
        MemberWeekAndMonthRankInfo weeklyRanking = fetchWeekAndMonthRankingByDate(
            accessMemberId, SummaryType.WEEK, today
        );
        MemberWeekAndMonthRankInfo monthlyRanking = fetchWeekAndMonthRankingByDate(
            accessMemberId, SummaryType.MONTH, today
        );

        return new MemberRankingResp(dailyRanking, weeklyRanking, monthlyRanking);
    }

    private MemberDayRankInfo fetchDailyRankingByDate(
        Long memberId, LocalDate today
    ) {
        return queryFactory
            .select(Projections.constructor(
                MemberDayRankInfo.class,
                competitionSummary.ranking,
                competitionSummary.elapsedTime,
                competitionSummary.solvedCount))
            .from(competitionSummary)
            .where(
                competitionSummary.member.id.eq(memberId),
                competitionSummary.type.eq(SummaryType.DAY),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today))
            .orderBy(competitionSummary.periodEnd.desc())
            .fetchOne();
    }

    private MemberWeekAndMonthRankInfo fetchWeekAndMonthRankingByDate(
        Long memberId, SummaryType type, LocalDate today
    ) {
        return queryFactory
            .select(Projections.constructor(
                MemberWeekAndMonthRankInfo.class,
                competitionSummary.ranking,
                competitionSummary.maxContinuousDays,
                competitionSummary.correctRate))
            .from(competitionSummary)
            .where(
                competitionSummary.member.id.eq(memberId),
                competitionSummary.type.eq(type),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today))
            .orderBy(competitionSummary.periodEnd.desc())
            .fetchOne();
    }

    private LocalDate latestDailyEnd(LocalDate now) {
        return queryFactory
            .select(competitionSummary.periodEnd.max())
            .from(competitionSummary)
            .where(
                competitionSummary.type.eq(SummaryType.DAY),
                competitionSummary.periodEnd.loe(now))
            .fetchOne();
    }

    private LocalDate currentPeriodEnd(SummaryType type, LocalDate now) {
        return queryFactory
            .select(competitionSummary.periodEnd)
            .from(competitionSummary)
            .where(
                competitionSummary.type.eq(type),
                competitionSummary.periodStart.loe(now),
                competitionSummary.periodEnd.goe(now))
            .fetchFirst();
    }

    private List<RankingInfoWithContinuousAndCorrectRate> fetchRankBySummaryTypeAndPeriodEnd(SummaryType summaryType, LocalDate periodEnd) {
        if (periodEnd == null) return List.of();

        return queryFactory
            .select(Projections.constructor(
                RankingInfoWithContinuousAndCorrectRate.class,
                member.nickname,
                member.imgUrl, // TODO : 추후 badge url로 변경
                member.imgUrl,
                competitionSummary.maxContinuousDays,
                competitionSummary.correctRate))
            .from(competitionSummary)
            .join(competitionSummary.member, member)
            .where(
                competitionSummary.type.eq(summaryType),
                competitionSummary.periodEnd.eq(periodEnd))
            .orderBy(competitionSummary.ranking.asc())
            .limit(10)
            .fetch();
    }
}
