package org.choon.careerbee.fixture.competition;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;
import static org.choon.careerbee.fixture.competition.CompetitionSummaryFixture.createSummary;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class RankingTestDataSupport {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionResultRepository competitionResultRepository;

    @Autowired
    private EntityManager em;
    private Map<Long, Member> memberMap = new HashMap<>();

    public Member prepareRankingData(LocalDate today) {
        Member me = null;
        for (int i = 1; i <= 10; i++) {
            Member member = memberRepository.saveAndFlush(
                createMember("testNick" + i, "test" + i + "@test.com", (long) i));

            if (i == 1) {
                me = member;
            }
            memberMap.putIfAbsent((long) i, member);
        }

        competitionRepository.saveAndFlush(
            createCompetition(
                LocalDateTime.of(2025, 6, 2, 20, 0, 0),
                LocalDateTime.of(2025, 6, 2, 20, 10, 0)
            )
        );

        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        for (int i = 1; i <= 10; i++) {
            short solvedCount = (short) (i % 5 + 1);
            int maxContinuousDays = i % 7 + 1;
            createSummary(
                memberMap.get((long) i), solvedCount, 123123L + i,
                (long) i, 1, (double) i * 10,
                SummaryType.DAY, today, today
            );

            createSummary(
                memberMap.get((long) i), solvedCount, 123123L + i,
                (long) i, maxContinuousDays, (double) i * 10,
                SummaryType.WEEK, weekStart, weekEnd
            );

            createSummary(
                memberMap.get((long) i), solvedCount, 123123L + i,
                (long) i, maxContinuousDays, (double) i * 10,
                SummaryType.MONTH, monthStart, monthEnd
            );
        }

        /** me 랭킹정보
         day : 1등, 123124, 2
         week: 1등, 2일연속, 10
         month: 1등, 2일연속, 10
         */
        return me;
    }

    public Member prepareLiveRankingData() {
        Competition competition = competitionRepository.saveAndFlush(createCompetition(
            LocalDateTime.of(2025, 6, 10, 13, 0, 0),
            LocalDateTime.of(2025, 6, 10, 13, 30, 0)
        ));

        Member me = null;
        List<CompetitionResult> results = new ArrayList<>();
        for (long i = 0; i < 12; i++) {
            Member member = memberRepository.saveAndFlush(
                createMember("testNick" + i, "test" + i + "@test.com", i));
            if (i == 0) {
                me = member;
            }

            competitionResultRepository.saveAndFlush(createCompetitionResult(
                competition, member, (short) 3, (int) (i + 10000)
            ));
        }
        updateCreatedAtOfCompetitionResults(results);

        /**
         me 실시간 랭킹정보 : 1등, 3개 solve, 10000 소요
         */
        return me;
    }

    private void updateCreatedAtOfCompetitionResults(List<CompetitionResult> results) {
        results.stream().forEach(result -> {
            em.createNativeQuery(
                    "UPDATE competition_result SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", LocalDateTime.of(2025, 6, 10, 13, 5))
                .setParameter("id", result.getId())
                .executeUpdate();
        });
    }

}
