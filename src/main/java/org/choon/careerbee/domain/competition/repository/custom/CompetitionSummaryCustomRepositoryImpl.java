package org.choon.careerbee.domain.competition.repository.custom;

import static org.choon.careerbee.domain.competition.domain.QCompetitionSummary.competitionSummary;
import static org.choon.careerbee.domain.member.entity.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfo;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfoWithContinuousAndCorrectRate;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
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
                member.imgUrl, // Todo : 추후 badge url도 가져와야함
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
            .map(tuple -> new RankingInfoWithContinuousAndCorrectRate(
                tuple.get(member.nickname),
                tuple.get(member.imgUrl), // Todo : 추후 뱃지 url로 변경
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
            .map(tuple -> new RankingInfoWithContinuousAndCorrectRate(
                tuple.get(member.nickname),
                tuple.get(member.imgUrl), // Todo : 추후 뱃지 url로 변경
                tuple.get(member.imgUrl),
                tuple.get(competitionSummary.maxContinuousDays),
                tuple.get(competitionSummary.correctRate)
            ))
            .toList();

        return new CompetitionRankingResp(daily, week, month);
    }

    @Override
    public MemberRankingResp fetchMemberRankingById(Long accessMemberId) {
        Long dailyRanking = fetchLatestRanking(accessMemberId, SummaryType.DAY);
        Long weeklyRanking = fetchLatestRanking(accessMemberId, SummaryType.WEEK);
        Long monthlyRanking = fetchLatestRanking(accessMemberId, SummaryType.MONTH);

        return new MemberRankingResp(dailyRanking, weeklyRanking, monthlyRanking);
    }

    private Long fetchLatestRanking(Long memberId, SummaryType type) {
        return queryFactory
            .select(competitionSummary.ranking)
            .from(competitionSummary)
            .where(
                competitionSummary.member.id.eq(memberId),
                competitionSummary.type.eq(type)
            )
            .orderBy(competitionSummary.periodEnd.desc())
            .limit(1)
            .fetchOne();
    }
}
