package org.choon.careerbee.domain.competition.repository.custom.result;

import static org.choon.careerbee.domain.competition.domain.QCompetitionResult.competitionResult;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.DateSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp.RankerInfo;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.ResultSummaryResp;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CompetitionResultCustomRepositoryImpl implements
    CompetitionResultCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DailyResultSummaryResp> fetchResultSummaryOfDaily(LocalDate summaryDate) {
        return queryFactory
            .select(Projections.constructor(
                DailyResultSummaryResp.class,
                competitionResult.member.id,
                competitionResult.solvedCount,
                competitionResult.elapsedTime
            ))
            .from(competitionResult)
            .where(
                competitionResult.createdAt.between(
                    summaryDate.atStartOfDay(),
                    summaryDate.atTime(LocalTime.MAX)
                )
            )
            .orderBy(
                competitionResult.solvedCount.desc(),
                competitionResult.elapsedTime.asc()
            )
            .fetch();
    }

    @Override
    public List<ResultSummaryResp> fetchResultSummaryByPeriod(SummaryPeriod summaryPeriod) {
        return queryFactory
            .select(Projections.constructor(
                ResultSummaryResp.class,
                competitionResult.member.id,
                competitionResult.solvedCount.sumLong(),
                competitionResult.elapsedTime.sumLong(),
                competitionResult.id.count()
            ))
            .from(competitionResult)
            .where(
                competitionResult.createdAt.goe(summaryPeriod.startAt().atStartOfDay())
                    .and(competitionResult.createdAt.lt(
                        summaryPeriod.endAt().plusDays(1).atStartOfDay())))
            .groupBy(competitionResult.member.id)
            .orderBy(
                competitionResult.solvedCount.sumLong().desc(),
                competitionResult.elapsedTime.sumLong().asc())
            .fetch();
    }

    @Override
    public List<DateSummaryResp> fetchDateSummaryIn(SummaryPeriod summaryPeriod,
        List<Long> summaryMemberIds) {
        return queryFactory
            .select(Projections.constructor(
                DateSummaryResp.class,
                competitionResult.member.id,
                competitionResult.createdAt
            ))
            .from(competitionResult)
            .where(
                competitionResult.member.id.in(summaryMemberIds),
                competitionResult.createdAt.goe(summaryPeriod.startAt().atStartOfDay())
                    .and(competitionResult.createdAt.lt(
                        summaryPeriod.endAt().plusDays(1).atStartOfDay()))
            )
            .orderBy(
                competitionResult.member.id.asc(),
                competitionResult.createdAt.asc())
            .fetch();
    }

    @Override
    public Optional<MemberLiveRankingResp> fetchMemberLiveRankingByDate(
        Long accessMemberId, LocalDate today
    ) {
        List<Tuple> todayCompetitionResults = queryFactory
            .select(
                competitionResult.member.id,
                competitionResult.solvedCount,
                competitionResult.elapsedTime
            )
            .from(competitionResult)
            .where(competitionResult.createdAt.year().eq(today.getYear())
                .and(competitionResult.createdAt.month().eq(today.getMonthValue()))
                .and(competitionResult.createdAt.dayOfMonth().eq(today.getDayOfMonth())))
            .orderBy(
                competitionResult.solvedCount.desc(),
                competitionResult.elapsedTime.asc())
            .fetch();

        Long rank = 1L;
        for (Tuple resp : todayCompetitionResults) {
            if (resp.get(competitionResult.member.id).equals(accessMemberId)) {
                return Optional.of(new MemberLiveRankingResp(
                    rank,
                    resp.get(competitionResult.elapsedTime),
                    resp.get(competitionResult.solvedCount)
                ));
            }
            rank++;
        }

        return Optional.empty();
    }

    @Override
    public LiveRankingResp fetchLiveRankingByDate(LocalDate today) {
        List<Tuple> top10 = queryFactory
            .select(
                competitionResult.member.id,
                competitionResult.member.nickname,
                competitionResult.member.imgUrl,
                competitionResult.solvedCount,
                competitionResult.elapsedTime
            )
            .from(competitionResult)
            .where(competitionResult.createdAt.year().eq(today.getYear())
                .and(competitionResult.createdAt.month().eq(today.getMonthValue()))
                .and(competitionResult.createdAt.dayOfMonth().eq(today.getDayOfMonth())))
            .orderBy(
                competitionResult.solvedCount.desc(),
                competitionResult.elapsedTime.asc())
            .limit(10)
            .fetch();

        List<RankerInfo> rankerInfos = new ArrayList<>();

        Long rank = 1L;
        for (Tuple tuple : top10) {
            rankerInfos.add(
                new RankerInfo(
                    rank,
                    tuple.get(competitionResult.member.nickname),
                    tuple.get(competitionResult.member.imgUrl),
                    tuple.get(competitionResult.elapsedTime),
                    tuple.get(competitionResult.solvedCount)
                )
            );
            rank++;
        }

        return new LiveRankingResp(rankerInfos);
    }
}
