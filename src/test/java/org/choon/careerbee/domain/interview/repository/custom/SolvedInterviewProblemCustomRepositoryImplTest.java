package org.choon.careerbee.domain.interview.repository.custom;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.interview.InterviewProblemFixture.createInterviewProblem;
import static org.choon.careerbee.fixture.interview.SolvedInterviewProblemFixture.createSolvedProblem;

import java.util.List;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp.SaveProblemInfo;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SolvedInterviewProblemCustomRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InterviewProblemRepository interviewProblemRepository;

    @Autowired
    private SolvedInterviewProblemRepository solvedInterviewProblemRepository;

    @Autowired
    private SolvedInterviewProblemCustomRepositoryImpl customRepository;

    @Test
    @DisplayName("회원이 저장한 면접 문제 조회 - 정상 동작")
    void fetchSaveProblemIdsByMemberId_success() {
        // given
        Member mockMember = memberRepository.save(createMember("testnick", "test@test.com", 1L));

        List<InterviewProblem> problems = interviewProblemRepository.saveAll(List.of(
            createInterviewProblem("백엔드 질문입니다", ProblemType.BACKEND),
            createInterviewProblem("프론트엔드 질문입니다", ProblemType.FRONTEND),
            createInterviewProblem("AI 질문입니다", ProblemType.AI),
            createInterviewProblem("데브옵스 질문입니다", ProblemType.DEVOPS)
        ));

        for (InterviewProblem problem : problems) {
            solvedInterviewProblemRepository.save(createSolvedProblem(
                mockMember, problem, "answer", "feedback", SaveStatus.SAVED
            ));
        }

        // when
        SaveInterviewProblemResp result = customRepository.fetchSaveProblemIdsByMemberId(
            mockMember.getId(), null, 10
        );

        // then
        assertThat(result.savedProblems()).hasSize(4);
        assertThat(result.savedProblems())
            .extracting("question")
            .contains("백엔드 질문입니다", "프론트엔드 질문입니다", "AI 질문입니다", "데브옵스 질문입니다");

        assertThat(result.savedProblems()).extracting("feedback")
            .containsOnly("feedback");

        assertThat(result.savedProblems()).extracting("answer")
            .containsOnly("answer");
    }

    @Test
    @DisplayName("커서 기반 페이징 - 다음 커서 기준으로 이후 데이터만 조회됨")
    void fetchSaveProblemIdsByMemberId_withCursor_success() {
        // given
        Member mockMember = memberRepository.save(createMember("testnick", "test@test.com", 2L));

        List<InterviewProblem> problems = interviewProblemRepository.saveAll(List.of(
            createInterviewProblem("문제1", ProblemType.BACKEND),
            createInterviewProblem("문제2", ProblemType.FRONTEND),
            createInterviewProblem("문제3", ProblemType.AI),
            createInterviewProblem("문제4", ProblemType.DEVOPS)
        ));

        List<SolvedInterviewProblem> solvedProblems = problems.stream()
            .map(problem -> createSolvedProblem(mockMember, problem, "answer", "feedback",
                SaveStatus.SAVED))
            .toList();

        solvedInterviewProblemRepository.saveAll(solvedProblems);

        // 최신 순 정렬 기준으로 first page 조회
        SaveInterviewProblemResp firstPage = customRepository.fetchSaveProblemIdsByMemberId(
            mockMember.getId(), null, 2
        );

        assertThat(firstPage.savedProblems()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        Long nextCursor = firstPage.nextCursor();

        // when: 두 번째 페이지 조회
        SaveInterviewProblemResp secondPage = customRepository.fetchSaveProblemIdsByMemberId(
            mockMember.getId(), nextCursor, 2
        );

        // then
        assertThat(secondPage.savedProblems()).hasSize(2);
        assertThat(secondPage.hasNext()).isFalse();

        List<String> remainingQuestions = secondPage.savedProblems().stream()
            .map(SaveProblemInfo::question)
            .toList();

        assertThat(remainingQuestions).containsExactlyInAnyOrder("문제1", "문제2");
    }

    @Test
    @DisplayName("저장된 면접 문제가 없을 경우 빈 리스트 반환")
    void fetchSaveProblemIdsByMemberId_empty() {
        // given
        Member member = memberRepository.save(createMember("emptyUser", "empty@test.com", 99L));

        // when
        SaveInterviewProblemResp resp = customRepository.fetchSaveProblemIdsByMemberId(
            member.getId(), null, 10
        );

        // then
        assertThat(resp.savedProblems()).isEmpty();
        assertThat(resp.hasNext()).isFalse();
        assertThat(resp.nextCursor()).isNull();
    }

}
