package org.choon.careerbee.domain.competition.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;
import static org.choon.careerbee.fixture.competition.CompetitionSummaryFixture.createSummary;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDSLConfig.class)
@ActiveProfiles("test")
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompetitionSummaryCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CompetitionSummaryCustomRepositoryImpl competitionSummaryCustomRepository;

    private void setCreatedAt(CompetitionResult result, LocalDateTime createdAt) {
        entityManager.createQuery(
                "update CompetitionResult r set r.createdAt = :createdAt where r.id = :id")
            .setParameter("createdAt", createdAt)
            .setParameter("id", result.getId())
            .executeUpdate();
    }

    @Test
    @DisplayName("fetchRankings()는 여러 멤버의 일/주/월 랭킹 데이터를 정렬 및 연속일 수와 함께 정상적으로 조회한다")
    void fetchRankings_shouldReturnValidDataWithMultipleMembers() {
        LocalDate today = LocalDate.of(2025, 6, 2); // 월요일

        Member member1 = em.persist(createMember("member1", "m1@test.com", 1L));
        Member member2 = em.persist(createMember("member2", "m2@test.com", 2L));
        Member member3 = em.persist(createMember("member3", "m3@test.com", 3L));

        Competition comp1 = em.persist(createCompetition(LocalDateTime.of(2025, 6, 1, 20, 0),
            LocalDateTime.of(2025, 6, 1, 20, 10)));
        Competition comp2 = em.persist(createCompetition(LocalDateTime.of(2025, 6, 2, 20, 0),
            LocalDateTime.of(2025, 6, 2, 20, 10)));
        Competition comp3 = em.persist(createCompetition(LocalDateTime.of(2025, 6, 3, 20, 0),
            LocalDateTime.of(2025, 6, 3, 20, 10)));

        // member1: 6/2, 6/3
        CompetitionResult r1 = em.persist(createCompetitionResult(comp2, member1,
            new CompetitionResultSubmitReq((short) 3, 1000)));
        CompetitionResult r2 = em.persist(createCompetitionResult(comp3, member1,
            new CompetitionResultSubmitReq((short) 3, 1000)));

        // member2: 6/1
        CompetitionResult r3 = em.persist(createCompetitionResult(comp1, member2,
            new CompetitionResultSubmitReq((short) 5, 2000)));

        // member3: 6/1, 6/2
        CompetitionResult r4 = em.persist(createCompetitionResult(comp1, member3,
            new CompetitionResultSubmitReq((short) 5, 1500)));
        CompetitionResult r5 = em.persist(createCompetitionResult(comp2, member3,
            new CompetitionResultSubmitReq((short) 5, 1500)));

        em.flush();

        setCreatedAt(r1, LocalDateTime.of(2025, 6, 2, 10, 0));
        setCreatedAt(r2, LocalDateTime.of(2025, 6, 3, 10, 0));
        setCreatedAt(r3, LocalDateTime.of(2025, 6, 1, 10, 0));
        setCreatedAt(r4, LocalDateTime.of(2025, 6, 1, 10, 0));
        setCreatedAt(r5, LocalDateTime.of(2025, 6, 2, 10, 0));

        em.clear();

        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        em.persist(createSummary(member2, (short) 5, 2000L, 2L, SummaryType.DAY, today, today));
        em.persist(createSummary(member3, (short) 5, 1500L, 1L, SummaryType.DAY, today, today));

        em.persist(
            createSummary(member1, (short) 6, 2000L, 1L, SummaryType.WEEK, weekStart, weekEnd));
        em.persist(
            createSummary(member3, (short) 10, 3000L, 2L, SummaryType.WEEK, weekStart, weekEnd));

        em.persist(
            createSummary(member1, (short) 6, 2000L, 1L, SummaryType.MONTH, monthStart, monthEnd));
        em.persist(
            createSummary(member3, (short) 10, 3000L, 2L, SummaryType.MONTH, monthStart, monthEnd));
        em.persist(
            createSummary(member2, (short) 5, 2000L, 3L, SummaryType.MONTH, monthStart, monthEnd));

        em.flush();
        em.clear();

        // when
        CompetitionRankingResp result = competitionSummaryCustomRepository.fetchRankings(today);

        // then
        assertThat(result.daily()).hasSize(2);
        assertThat(result.daily().get(0).nickname()).isEqualTo("member3");
        assertThat(result.daily().get(1).nickname()).isEqualTo("member2");

        assertThat(result.week()).hasSize(2);
        assertThat(result.week().get(0).nickname()).isEqualTo("member1");
        assertThat(result.week().get(0).continuous()).isEqualTo(2); // 6/2, 6/3
        assertThat(result.week().get(1).continuous()).isEqualTo(2); // 6/1, 6/2

        assertThat(result.month()).hasSize(3);
        assertThat(result.month().get(0).nickname()).isEqualTo("member1");
        assertThat(result.month().get(0).continuous()).isEqualTo(2);
        assertThat(result.month().get(2).nickname()).isEqualTo("member2");
        assertThat(result.month().get(2).continuous()).isEqualTo(1);
    }

    @Test
    @DisplayName("fetchRankings()는 조건에 맞는 데이터가 없으면 빈 리스트를 반환한다")
    void fetchRankings_shouldReturnEmptyListsWhenNoData() {
        LocalDate today = LocalDate.of(2025, 6, 2);

        CompetitionRankingResp result = competitionSummaryCustomRepository.fetchRankings(today);

        assertThat(result.daily()).isEmpty();
        assertThat(result.week()).isEmpty();
        assertThat(result.month()).isEmpty();
    }
}
