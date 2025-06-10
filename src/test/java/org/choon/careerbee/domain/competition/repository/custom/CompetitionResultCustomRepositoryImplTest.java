package org.choon.careerbee.domain.competition.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
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
class CompetitionResultCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CompetitionResultCustomRepositoryImpl competitionResultCustomRepository;

    @Test
    @DisplayName("오늘 날짜의 대회 결과 중 특정 유저의 실시간 랭킹 조회 성공")
    void fetchMemberLiveRankingByDate_success() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 10);

        Member me = em.persist(createMember("유저1", "user1@test.com", 1L));
        Member member2 = em.persist(createMember("유저2", "user2@test.com", 2L));
        Member member3 = em.persist(createMember("유저3", "user3@test.com", 3L));

        Competition todayCompetition = em.persist(createCompetition(
            LocalDateTime.of(2025, 6, 10, 13, 0, 0),
            LocalDateTime.of(2025, 6, 10, 13, 10, 0)
        ));

        em.persist(
            createCompetitionResult(todayCompetition, me, new CompetitionResultSubmitReq(
                (short) 3, 100000
            )));
        em.persist(
            createCompetitionResult(todayCompetition, member2, new CompetitionResultSubmitReq(
                (short) 3, 90000
            )));
        em.persist(
            createCompetitionResult(todayCompetition, member3, new CompetitionResultSubmitReq(
                (short) 4, 200000
            )));

        em.flush();
        em.clear();

        // when
        Optional<MemberLiveRankingResp> result =
            competitionResultCustomRepository.fetchMemberLiveRankingByDate(me.getId(), today);

        // then
        assertThat(result).isPresent();
        MemberLiveRankingResp rankingResp = result.get();
        assertThat(rankingResp.rank()).isEqualTo(3L);
        assertThat(rankingResp.solvedCount()).isEqualTo((short) 3);
        assertThat(rankingResp.elapsedTime()).isEqualTo(100000);
    }

    @Test
    @DisplayName("오늘 날짜의 대회 결과에 존재하지 않는 유저일 경우 Optional.empty 반환")
    void fetchMemberLiveRankingByDate_notExists_returnsEmpty() {
        // given
        LocalDate today = LocalDate.now();

        Member member1 = em.persist(createMember("유저1", "user1@test.com", 1L));

        Competition yesterdayCompetition = em.persist(createCompetition(
            LocalDateTime.of(2025, 6, 9, 13, 0, 0),
            LocalDateTime.of(2025, 6, 9, 13, 10, 0)
        ));

        em.persist(
            createCompetitionResult(yesterdayCompetition, member1, new CompetitionResultSubmitReq(
                (short) 3, 100000
            )));

        Member unknown = em.persist(createMember("없는유저", "no@test.com", 2L));

        em.flush();
        em.clear();

        // when
        Optional<MemberLiveRankingResp> result =
            competitionResultCustomRepository.fetchMemberLiveRankingByDate(unknown.getId(), today);

        // then
        assertThat(result).isEmpty();
    }
}
