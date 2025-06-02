package org.choon.careerbee.domain.competition.repository.custom;

import static org.choon.careerbee.domain.competition.domain.QCompetitionResult.competitionResult;
import static org.choon.careerbee.domain.competition.domain.QCompetitionSummary.competitionSummary;
import static org.choon.careerbee.domain.member.entity.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfo;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp.RankingInfoWithContinuous;
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
                member.imgUrl,
                competitionSummary.solvedCount
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

        List<RankingInfoWithContinuous> week = rawWeekResults.stream()
            .map(tuple -> {
                Long memberId = tuple.get(member.id);
                int continuous = calculateMaxContinuousDaysByMemberId(memberId,
                    today.atStartOfDay());
                return new RankingInfoWithContinuous(
                    tuple.get(member.nickname),
                    tuple.get(member.imgUrl),
                    tuple.get(member.imgUrl),
                    continuous,
                    tuple.get(competitionSummary.solvedCount)
                );
            })
            .toList();

        List<Tuple> rawMonthResults = queryFactory
            .select(
                member.id,
                member.nickname,
                member.imgUrl,
                competitionSummary.solvedCount
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

        List<RankingInfoWithContinuous> month = rawMonthResults.stream()
            .map(tuple -> {
                Long memberId = tuple.get(member.id);

                return new RankingInfoWithContinuous(
                    tuple.get(member.nickname),
                    tuple.get(member.imgUrl),
                    tuple.get(member.imgUrl),
                    calculateMaxContinuousDaysByMemberId(memberId, today.atStartOfDay()),
                    tuple.get(competitionSummary.solvedCount)
                );
            })
            .toList();

        return new CompetitionRankingResp(daily, week, month);
    }

    public int calculateMaxContinuousDaysByMemberId(Long memberId, LocalDateTime today) {
        LocalDateTime startOfMonth = today.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.toLocalDate().lengthOfMonth())
            .toLocalDate()
            .atTime(23, 59, 59);

        Set<LocalDate> participatedDays = queryFactory
            .select(competitionResult.createdAt)
            .from(competitionResult)
            .where(
                competitionResult.member.id.eq(memberId),
                competitionResult.createdAt.goe(startOfMonth),
                competitionResult.createdAt.loe(endOfMonth))
            .orderBy(competitionResult.createdAt.asc())
            .fetch()
            .stream()
            .map(dateTime -> {
                LocalDate date = dateTime.toLocalDate();
                System.out.println("참여 날짜: " + date);  // ✅ 이거 반드시 찍어보세요
                return date;
            })
            .collect(Collectors.toSet());

        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate checkDate = startOfMonth.toLocalDate();

        while (!checkDate.isAfter(endOfMonth.toLocalDate())) {
            if (participatedDays.contains(checkDate)) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            checkDate = checkDate.plusDays(1);
        }

        return maxStreak;
    }
}
