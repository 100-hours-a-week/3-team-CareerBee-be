package org.choon.careerbee.domain.competition.repository.custom.summary;

import static org.choon.careerbee.domain.competition.domain.QCompetitionSummary.competitionSummary;
import static org.choon.careerbee.domain.member.entity.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfo;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfoWithContinuousAndCorrectRate;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp.MemberDayRankInfo;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp.MemberWeekAndMonthRankInfo;
import org.choon.careerbee.domain.competition.dto.response.Top10Info;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CompetitionSummaryCustomRepositoryImpl implements
    CompetitionSummaryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CompetitionRankingResp fetchRankings(LocalDate today) {
        List<RankingInfo> daily = queryFactory
            .select(Projections.constructor(
                RankingInfo.class,
                member.nickname,
                member.imgUrl,
                competitionSummary.elapsedTime,
                competitionSummary.solvedCount
            ))
            .from(competitionSummary)
            .join(competitionSummary.member, member)
            .where(
                competitionSummary.type.eq(SummaryType.DAY),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today)
            )
            .orderBy(competitionSummary.ranking.asc())
            .limit(10)
            .fetch();

        List<Tuple> rawWeekResults = queryFactory
            .select(
                member.id,
                member.nickname,
                member.imgUrl,
                competitionSummary.correctRate,
                competitionSummary.maxContinuousDays
            )
            .from(competitionSummary)
            .join(competitionSummary.member, member)
            .where(
                competitionSummary.type.eq(SummaryType.WEEK),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today)
            )
            .orderBy(competitionSummary.ranking.asc())
            .limit(10)
            .fetch();

        List<RankingInfoWithContinuousAndCorrectRate> week = rawWeekResults.stream()
            .map(tuple -> RankingInfoWithContinuousAndCorrectRate.from(
                tuple.get(member.nickname),
                tuple.get(member.imgUrl),
                tuple.get(competitionSummary.maxContinuousDays),
                tuple.get(competitionSummary.correctRate)
            ))
            .toList();

        List<Tuple> rawMonthResults = queryFactory
            .select(
                member.id,
                member.nickname,
                member.imgUrl,
                competitionSummary.correctRate,
                competitionSummary.maxContinuousDays
            )
            .from(competitionSummary)
            .join(competitionSummary.member, member)
            .where(
                competitionSummary.type.eq(SummaryType.MONTH),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today)
            )
            .orderBy(competitionSummary.ranking.asc())
            .limit(10)
            .fetch();

        List<RankingInfoWithContinuousAndCorrectRate> month = rawMonthResults.stream()
            .map(tuple -> RankingInfoWithContinuousAndCorrectRate.from(
                tuple.get(member.nickname),
                tuple.get(member.imgUrl),
                tuple.get(competitionSummary.maxContinuousDays),
                tuple.get(competitionSummary.correctRate)
            ))
            .toList();

        return new CompetitionRankingResp(daily, week, month);
    }

    @Override
    public MemberRankingResp fetchMemberRankingById(Long accessMemberId, LocalDate today) {
        MemberDayRankInfo dailyRanking = fetchDailyRankingByDate(
            accessMemberId, SummaryType.DAY, today
        );
        MemberWeekAndMonthRankInfo weeklyRanking = fetchWeekAndMonthRankingByDate(
            accessMemberId, SummaryType.WEEK, today
        );
        MemberWeekAndMonthRankInfo monthlyRanking = fetchWeekAndMonthRankingByDate(
            accessMemberId, SummaryType.MONTH, today
        );

        return new MemberRankingResp(dailyRanking, weeklyRanking, monthlyRanking);
    }

    @Override
    public List<CompetitionSummary> fetchSummaryByPeriodAndType(
        SummaryPeriod summaryPeriod, SummaryType summaryType
    ) {
        return queryFactory
            .selectFrom(competitionSummary)
            .where(
                competitionSummary.type.eq(summaryType),
                competitionSummary.periodStart.eq(summaryPeriod.startAt()),
                competitionSummary.periodEnd.eq(summaryPeriod.endAt()))
            .fetch();
    }

    @Override
    public List<Top10Info> fetchTop10Ranker(
        SummaryPeriod summaryPeriod, SummaryType summaryType
    ) {
        return queryFactory
            .select(Projections.constructor(
                Top10Info.class,
                competitionSummary.ranking,
                competitionSummary.member
            ))
            .from(competitionSummary)
            .where(
                competitionSummary.periodStart.eq(summaryPeriod.startAt()),
                competitionSummary.periodEnd.eq(summaryPeriod.endAt()),
                competitionSummary.type.eq(summaryType)
            )
            .limit(10)
            .fetch();
    }

    private MemberDayRankInfo fetchDailyRankingByDate(
        Long memberId, SummaryType type, LocalDate today
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
                competitionSummary.type.eq(type),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today)
            )
            .orderBy(competitionSummary.periodEnd.desc())
            .fetchOne();
    }

    private MemberWeekAndMonthRankInfo fetchWeekAndMonthRankingByDate(
        Long memberId, SummaryType type, LocalDate today
    ) {
        Tuple result = queryFactory
            .select(
                competitionSummary.ranking,
                competitionSummary.maxContinuousDays,
                competitionSummary.correctRate
            )
            .from(competitionSummary)
            .where(
                competitionSummary.member.id.eq(memberId),
                competitionSummary.type.eq(type),
                competitionSummary.periodStart.loe(today),
                competitionSummary.periodEnd.goe(today)
            )
            .orderBy(competitionSummary.periodEnd.desc())
            .fetchOne();

        if (result == null) {
            return null;
        }

        return MemberWeekAndMonthRankInfo.from(
            result.get(competitionSummary.ranking),
            result.get(competitionSummary.maxContinuousDays),
            result.get(competitionSummary.correctRate)
        );
    }
}
