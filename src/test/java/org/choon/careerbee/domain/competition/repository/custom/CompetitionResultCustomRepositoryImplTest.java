package org.choon.careerbee.domain.competition.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp.RankerInfo;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.repository.custom.result.CompetitionResultCustomRepositoryImpl;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.fixture.competition.RankingTestDataSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
class CompetitionResultCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CompetitionResultCustomRepositoryImpl competitionResultCustomRepository;

    @Autowired
    private RankingTestDataSupport testDataSupport;

    @Test
    @DisplayName("오늘 날짜의 대회 결과 중 특정 유저의 실시간 랭킹 조회 성공")
    void fetchMemberLiveRankingByDate_success() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 10);

        Member me = testDataSupport.prepareLiveRankingData();

        // when
        Optional<MemberLiveRankingResp> result =
            competitionResultCustomRepository.fetchMemberLiveRankingByDate(me.getId(), today);

        // then
        assertThat(result).isPresent();
        MemberLiveRankingResp rankingResp = result.get();
        assertThat(rankingResp.rank()).isEqualTo(1L);
        assertThat(rankingResp.solvedCount()).isEqualTo((short) 3);
        assertThat(rankingResp.elapsedTime()).isEqualTo(10000);
    }

    @Test
    @DisplayName("오늘 날짜의 대회 결과에 존재하지 않는 유저일 경우 Optional.empty 반환")
    void fetchMemberLiveRankingByDate_notExists_returnsEmpty() {
        // given
        LocalDate today = LocalDate.now();
        Member unknown = em.persist(createMember("없는유저", "no@test.com", 2L));

        em.flush();
        em.clear();

        // when
        Optional<MemberLiveRankingResp> result =
            competitionResultCustomRepository.fetchMemberLiveRankingByDate(unknown.getId(), today);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("오늘 날짜 기준 상위 10명 실시간 랭킹 조회")
    void fetchLiveRankingByDate_success_withMap() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 10);

        testDataSupport.prepareLiveRankingData();

        // when
        LiveRankingResp result = competitionResultCustomRepository.fetchLiveRankingByDate(today);

        // then
        assertThat(result).isNotNull();
        List<RankerInfo> rankers = result.rankings();
        assertThat(rankers).hasSize(10);

        for (int i = 0; i < 10; i++) {
            assertThat(rankers.get(i).nickname()).isEqualTo("testNick" + i);
            assertThat(rankers.get(i).rank()).isEqualTo(i + 1L);
        }
    }

}
