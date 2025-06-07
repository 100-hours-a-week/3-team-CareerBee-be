package org.choon.careerbee.domain.competition.repository.custom.result;

import static org.choon.careerbee.domain.competition.domain.QCompetitionResult.competitionResult;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
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
}
