package org.choon.careerbee.fixture.competition;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;
import static org.choon.careerbee.fixture.competition.CompetitionSummaryFixture.createSummary;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.member.entity.Member;

public class RankingTestDataSupport {

    private final EntityManager em;
    private final Map<String, Member> memberMap = new HashMap<>();

    public RankingTestDataSupport(EntityManager em) {
        this.em = em;
    }

    public void prepareRankingData(LocalDate today) {
        Member member1 = em.merge(createMember("member1", "m1@test.com", 1L));
        Member member2 = em.merge(createMember("member2", "m2@test.com", 2L));
        Member member3 = em.merge(createMember("member3", "m3@test.com", 3L));

        memberMap.put("member1", member1);
        memberMap.put("member2", member2);
        memberMap.put("member3", member3);

        Competition comp1 = em.merge(createCompetition(LocalDateTime.of(2025, 6, 1, 20, 0),
            LocalDateTime.of(2025, 6, 1, 20, 10)));
        Competition comp2 = em.merge(createCompetition(LocalDateTime.of(2025, 6, 2, 20, 0),
            LocalDateTime.of(2025, 6, 2, 20, 10)));
        Competition comp3 = em.merge(createCompetition(LocalDateTime.of(2025, 6, 3, 20, 0),
            LocalDateTime.of(2025, 6, 3, 20, 10)));

        CompetitionResult r1 = em.merge(createCompetitionResult(comp2, member1,
            new CompetitionResultSubmitReq((short) 3, 1000)));
        CompetitionResult r2 = em.merge(createCompetitionResult(comp3, member1,
            new CompetitionResultSubmitReq((short) 3, 1000)));
        CompetitionResult r3 = em.merge(createCompetitionResult(comp1, member2,
            new CompetitionResultSubmitReq((short) 5, 2000)));
        CompetitionResult r4 = em.merge(createCompetitionResult(comp1, member3,
            new CompetitionResultSubmitReq((short) 5, 1500)));
        CompetitionResult r5 = em.merge(createCompetitionResult(comp2, member3,
            new CompetitionResultSubmitReq((short) 5, 1500)));

        em.flush();

        updateCreatedAt(r1, LocalDateTime.of(2025, 6, 2, 10, 0));
        updateCreatedAt(r2, LocalDateTime.of(2025, 6, 3, 10, 0));
        updateCreatedAt(r3, LocalDateTime.of(2025, 6, 1, 10, 0));
        updateCreatedAt(r4, LocalDateTime.of(2025, 6, 1, 10, 0));
        updateCreatedAt(r5, LocalDateTime.of(2025, 6, 2, 10, 0));

        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        em.merge(createSummary(
            member2, (short) 5, 2000L,
            2L, 1, 80.0,
            SummaryType.DAY, today, today
        ));
        em.merge(createSummary(
            member3, (short) 5, 1500L,
            1L, 2, 75.0,
            SummaryType.DAY, today, today
        ));

        em.merge(createSummary(
            member1, (short) 6, 2000L,
            1L, 4, 88.0,
            SummaryType.WEEK, weekStart, weekEnd
        ));
        em.merge(createSummary(
            member3, (short) 10, 3000L,
            2L, 6, 88.0,
            SummaryType.WEEK, weekStart, weekEnd
        ));

        em.merge(createSummary(
            member1, (short) 6, 2000L,
            1L, 22, 99.9,
            SummaryType.MONTH, monthStart, monthEnd
        ));
        em.merge(createSummary(
            member3, (short) 10, 3000L,
            2L, 12, 79.0,
            SummaryType.MONTH, monthStart, monthEnd));
        em.merge(createSummary(
            member2, (short) 5, 2000L,
            3L, 18, 88.0,
            SummaryType.MONTH, monthStart, monthEnd
        ));

        em.flush();
        em.clear();
    }

    public Long getMemberId(String nickname) {
        Member member = memberMap.get(nickname);
        if (member == null) {
            throw new IllegalArgumentException("해당 nickname에 대한 멤버가 없습니다: " + nickname);
        }
        return member.getId();
    }

    private void updateCreatedAt(CompetitionResult result, LocalDateTime createdAt) {
        em.createQuery("update CompetitionResult r set r.createdAt = :createdAt where r.id = :id")
            .setParameter("createdAt", createdAt)
            .setParameter("id", result.getId())
            .executeUpdate();
    }

}
