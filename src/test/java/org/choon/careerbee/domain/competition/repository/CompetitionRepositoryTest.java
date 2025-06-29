package org.choon.careerbee.domain.competition.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;

import java.time.LocalDateTime;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
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
class CompetitionRepositoryTest {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Test
    @DisplayName("이미 대회에 참여했다면 true 반환")
    void existsByMemberIdAndCompetitionId_true_whenAlreadyJoined() {
        // given
        Member member = memberRepository.save(createMember("testNick", "test1@test.com", 1234L));
        Competition competition = competitionRepository.save(createCompetition(
            LocalDateTime.of(2025, 5, 30, 20, 0, 0),
            LocalDateTime.of(2025, 5, 30, 20, 10, 0)
        ));

        competitionParticipantRepository.save(
            CompetitionParticipant.of(member, competition)
        );

        // when
        boolean exists = competitionParticipantRepository.existsByMemberIdAndCompetitionId(
            member.getId(),
            competition.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("대회에 참여한적 없다면 false 반환")
    void existsByMemberIdAndCompetitionId_false_whenFirstJoined() {
        // given
        Member member = memberRepository.save(createMember("testNick", "test2@test.com", 123L));
        Competition competition = competitionRepository.save(createCompetition(
            LocalDateTime.of(2025, 5, 30, 20, 0, 0),
            LocalDateTime.of(2025, 5, 30, 20, 10, 0)
        ));

        // when
        boolean exists = competitionParticipantRepository.existsByMemberIdAndCompetitionId(
            member.getId(),
            competition.getId()
        );

        // then
        assertThat(exists).isFalse();
    }
}
