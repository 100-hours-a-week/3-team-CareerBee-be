package org.choon.careerbee.domain.competition.repository.custom;

import static org.choon.careerbee.domain.competition.domain.QCompetitionResult.competitionResult;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CompetitionResultCustomRepositoryImpl implements CompetitionResultCustomRepository {

    private final JPAQueryFactory queryFactory;

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
}
