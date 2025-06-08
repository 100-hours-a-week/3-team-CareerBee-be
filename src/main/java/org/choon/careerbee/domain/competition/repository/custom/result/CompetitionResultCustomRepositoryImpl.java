package org.choon.careerbee.domain.competition.repository.custom.result;

import static org.choon.careerbee.domain.competition.domain.QCompetitionResult.competitionResult;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.DateSummaryResp;
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
    public List<DateSummaryResp> fetchDateSummaryIn(SummaryPeriod summaryPeriod, List<Long> summaryMemberIds) {
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
}
