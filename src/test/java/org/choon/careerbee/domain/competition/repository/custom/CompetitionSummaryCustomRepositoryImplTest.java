package org.choon.careerbee.domain.competition.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionSummaryFixture.createSummary;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.repository.custom.summary.CompetitionSummaryCustomRepositoryImpl;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.fixture.competition.RankingTestDataSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDSLConfig.class)
@ActiveProfiles("test")
@DataJpaTest(
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = RankingTestDataSupport.class))
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompetitionSummaryCustomRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CompetitionSummaryCustomRepositoryImpl competitionSummaryCustomRepository;

    @Autowired
    private RankingTestDataSupport testDataSupport;

    @Test
    @DisplayName("랭킹 조회 성공")
    void fetchRankings_serviceTest() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2); // 월요일
        testDataSupport.prepareRankingData(today);

        // when
        CompetitionRankingResp result = competitionSummaryCustomRepository.fetchRankings(today);

        // then
        assertThat(result.daily()).hasSize(10);
        assertThat(result.week().get(0).continuous()).isEqualTo(2);
        assertThat(result.month()).hasSize(10);
    }

    @Test
    @DisplayName("랭킹 조회시 데이터가 없다면 빈 리스트 반환")
    void fetchRankings_shouldReturnEmptyListsWhenNoData() {
        LocalDate today = LocalDate.of(2025, 6, 2);

        CompetitionRankingResp result = competitionSummaryCustomRepository.fetchRankings(today);

        assertThat(result.daily()).isEmpty();
        assertThat(result.week()).isEmpty();
        assertThat(result.month()).isEmpty();
    }

    @Test
    @DisplayName("특정 멤버의 랭킹 조회 성공")
    void fetchMemberRankingById_shouldReturnLatestRankingDirectlyCreated() {
        // given
        Member member = em.merge(createMember("test_user", "test@a.com", 10L));

        LocalDate today = LocalDate.of(2025, 6, 2); // 월요일
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        // 가장 최신 summary 데이터 생성
        em.persist(
            createSummary(
                member, (short) 4, 1000L,
                1L, 1, 90.0,
                SummaryType.DAY, today, today)
        );
        em.persist(
            createSummary(
                member, (short) 8, 2000L,
                2L, 5, 70.0,
                SummaryType.WEEK, weekStart, weekEnd)
        );
        em.persist(
            createSummary(member, (short) 10, 3000L,
                3L, 18, 70.6,
                SummaryType.MONTH, monthStart, monthEnd)
        );
        em.flush();
        em.clear();

        // when
        Long memberId = member.getId();
        var result = competitionSummaryCustomRepository.fetchMemberRankingById(memberId, today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.daily().rank()).isEqualTo(1L);
        assertThat(result.week().rank()).isEqualTo(2L);
        assertThat(result.month().rank()).isEqualTo(3L);
    }
}
