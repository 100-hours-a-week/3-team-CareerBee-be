package org.choon.careerbee.domain.competition.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;

import java.time.LocalDateTime;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDSLConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class CompetitionResultRepositoryTest {

    @Autowired
    private CompetitionResultRepository competitionResultRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이미 대회 결과를 제출했다면 true 반환")
    void existsByMemberIdAndCompetitionId_true_whenSubmitted() {
        // given
        Member member = memberRepository.save(createMember("resultNick", "res@test.com", 321L));
        Competition competition = competitionRepository.save(createCompetition(
            LocalDateTime.of(2025, 5, 30, 20, 0, 0),
            LocalDateTime.of(2025, 5, 30, 20, 10, 0)
        ));

        CompetitionResult result = createCompetitionResult(
            competition, member, (short) 5, 123123
        );
        competitionResultRepository.save(result);

        // when
        boolean exists = competitionResultRepository.existsByMemberIdAndCompetitionId(
            member.getId(), competition.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("대회 결과를 제출한 적 없다면 false 반환")
    void existsByMemberIdAndCompetitionId_false_whenFirstSubmit() {
        // given
        Member member = memberRepository.save(createMember("newbie", "new@test.com", 456L));
        Competition competition = competitionRepository.save(createCompetition(
            LocalDateTime.of(2025, 5, 30, 20, 0, 0),
            LocalDateTime.of(2025, 5, 30, 20, 10, 0)
        ));

        // when
        boolean exists = competitionResultRepository.existsByMemberIdAndCompetitionId(
            member.getId(), competition.getId());

        // then
        assertThat(exists).isFalse();
    }
}
