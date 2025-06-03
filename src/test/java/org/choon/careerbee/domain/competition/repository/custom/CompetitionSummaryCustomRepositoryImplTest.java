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
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.fixture.competition.RankingTestDataSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDSLConfig.class)
@ActiveProfiles("test")
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompetitionSummaryCustomRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CompetitionSummaryCustomRepositoryImpl competitionSummaryCustomRepository;

    private RankingTestDataSupport testDataSupport;

    @BeforeEach
    void setUp() {
        testDataSupport = new RankingTestDataSupport(em);
    }

    @Test
    @DisplayName("랭킹 조회 성공")
    void fetchRankings_serviceTest() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2); // 월요일
        testDataSupport.prepareRankingData(today);

        // when
        CompetitionRankingResp result = competitionSummaryCustomRepository.fetchRankings(today);

        // then
        assertThat(result.daily()).hasSize(2);
        assertThat(result.week().get(0).continuous()).isEqualTo(2);
        assertThat(result.month()).hasSize(3);
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
        em.merge(createSummary(member, (short) 5, 1200L, 1L, SummaryType.DAY, today, today));
        em.merge(
            createSummary(member, (short) 10, 3000L, 2L, SummaryType.WEEK, weekStart, weekEnd));
        em.merge(
            createSummary(member, (short) 15, 4000L, 3L, SummaryType.MONTH, monthStart, monthEnd));
        em.flush();
        em.clear();

        // when
        Long memberId = member.getId();
        var result = competitionSummaryCustomRepository.fetchMemberRankingById(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.day()).isEqualTo(1L);
        assertThat(result.week()).isEqualTo(2L);
        assertThat(result.month()).isEqualTo(3L);
    }
}
