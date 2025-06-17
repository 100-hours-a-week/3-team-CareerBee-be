package org.choon.careerbee.domain.competition.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionProblemFixture.createProblem;
import static org.choon.careerbee.fixture.competition.ProblemChoiceFixture.createProblemChoice;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.problem.CompetitionProblem;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.repository.custom.competition.CompetitionCustomRepositoryImpl;
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
class CompetitionCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CompetitionCustomRepositoryImpl competitionCustomRepository;

    @Test
    @DisplayName("대회 ID로 문제 및 보기 정상 조회")
    void fetchCompetitionProblemsByCompetitionId_shouldReturnProblemWithChoices() {
        // given
        Competition competition = createCompetition(
            LocalDateTime.of(2025, 5, 30, 20, 0),
            LocalDateTime.of(2025, 5, 30, 20, 10)
        );
        em.persist(competition);

        CompetitionProblem problem1 = createProblem(competition, "제목1", "설명1", "해설1", (short) 2);
        CompetitionProblem problem2 = createProblem(competition, "제목2", "설명2", "해설2", (short) 1);
        em.persist(problem1);
        em.persist(problem2);

        em.persist(createProblemChoice(problem1, "1번 보기", (short) 1));
        em.persist(createProblemChoice(problem1, "2번 보기", (short) 2));
        em.persist(createProblemChoice(problem2, "1번 보기", (short) 1));

        em.flush();
        em.clear();

        // when
        CompetitionProblemResp result =
            competitionCustomRepository.fetchCompetitionProblemsByCompetitionId(
                competition.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.problems()).hasSize(2);

        var p1 = result.problems().get(0);
        assertThat(p1.title()).isEqualTo("제목1");
        assertThat(p1.choices()).extracting("content")
            .containsExactly("1번 보기", "2번 보기");

        var p2 = result.problems().get(1);
        assertThat(p2.title()).isEqualTo("제목2");
        assertThat(p2.choices()).extracting("content")
            .containsExactly("1번 보기");
    }

    @Test
    @DisplayName("대회 ID로 조회했을 때 문제가 없으면 빈 리스트 반환")
    void fetchCompetitionProblemsByCompetitionId_shouldReturnEmptyListWhenNoProblems() {
        // given
        Competition competition = createCompetition(
            LocalDateTime.of(2025, 5, 30, 20, 0),
            LocalDateTime.of(2025, 5, 30, 20, 10)
        );
        em.persist(competition);
        em.flush();
        em.clear();

        // when
        CompetitionProblemResp result =
            competitionCustomRepository.fetchCompetitionProblemsByCompetitionId(
                competition.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.problems()).isEmpty();
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 대회가 존재할 경우")
    void fetchCompetitionIdFromToday_shouldReturnId_whenCompetitionExists() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 6, 2, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 2, 12, 0);
        Competition competition = createCompetition(start, end);
        em.persist(competition);
        em.flush();
        em.clear();

        // when
        CompetitionIdResp result = competitionCustomRepository.fetchCompetitionIdFromToday(
            LocalDate.of(2025, 6, 2));

        // then
        assertThat(result).isNotNull();
        assertThat(result.competitionId()).isEqualTo(competition.getId());
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 대회가 존재하지 않을 경우 null 반환")
    void fetchCompetitionIdFromToday_shouldReturnNull_whenNoCompetitionExists() {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        // 대회 없음

        // when
        CompetitionIdResp result = competitionCustomRepository.fetchCompetitionIdFromToday(today);

        // then
        assertThat(result).isNull();
    }
}
