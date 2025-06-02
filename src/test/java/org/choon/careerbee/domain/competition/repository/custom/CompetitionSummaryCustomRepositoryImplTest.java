package org.choon.careerbee.domain.competition.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
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
    @DisplayName("랭킹 조회 성공 - 서비스 레벨에서")
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
    @DisplayName("fetchRankings()는 조건에 맞는 데이터가 없으면 빈 리스트를 반환한다")
    void fetchRankings_shouldReturnEmptyListsWhenNoData() {
        LocalDate today = LocalDate.of(2025, 6, 2);

        CompetitionRankingResp result = competitionSummaryCustomRepository.fetchRankings(today);

        assertThat(result.daily()).isEmpty();
        assertThat(result.week()).isEmpty();
        assertThat(result.month()).isEmpty();
    }
}
