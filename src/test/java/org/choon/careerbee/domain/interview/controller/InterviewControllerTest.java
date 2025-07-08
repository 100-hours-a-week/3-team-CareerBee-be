package org.choon.careerbee.domain.interview.controller;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.interview.InterviewProblemFixture.createInterviewProblem;
import static org.choon.careerbee.fixture.interview.SolvedInterviewProblemFixture.createSolvedProblem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InterviewProblemRepository interviewProblemRepository;

    @Autowired
    private SolvedInterviewProblemRepository solvedProblemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Member testMember;
    private String accessToken;

    @BeforeEach
    void setUp() {
        solvedProblemRepository.deleteAllInBatch();
        interviewProblemRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        testMember = memberRepository.save(createMember("solveUser", "solve@bee.com", 33L));
        accessToken = "Bearer " + jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("면접 문제 조회 API - 각 타입별 첫 번째 문제 조회 성공")
    void fetchInterviewProblem_success() throws Exception {
        // then
        interviewProblemRepository.saveAll(List.of(
            createInterviewProblem("백엔드 질문입니다", ProblemType.BACKEND),
            createInterviewProblem("프론트엔드 질문입니다", ProblemType.FRONTEND),
            createInterviewProblem("AI 질문입니다", ProblemType.AI),
            createInterviewProblem("데브옵스 질문입니다", ProblemType.DEVOPS)
        ));

        // when & then
        mockMvc.perform(get("/api/v1/interview-problems")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value("면접문제 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.interviewProblems").isArray())
            .andExpect(jsonPath("$.data.interviewProblems.length()").value(4))
            .andExpect(jsonPath("$.data.interviewProblems[?(@.type == 'BACKEND')].question")
                .value("백엔드 질문입니다"))
            .andExpect(jsonPath("$.data.interviewProblems[?(@.type == 'FRONTEND')].question")
                .value("프론트엔드 질문입니다"))
            .andExpect(jsonPath("$.data.interviewProblems[?(@.type == 'AI')].question")
                .value("AI 질문입니다"))
            .andExpect(jsonPath("$.data.interviewProblems[?(@.type == 'DEVOPS')].question")
                .value("데브옵스 질문입니다"));
    }

    @Test
    @DisplayName("면접문제 풀이 여부 조회 - 사용자가 문제를 푼 경우 true 반환")
    void checkInterviewProblemSolved_true() throws Exception {
        // given
        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("문제1", ProblemType.BACKEND)
        );

        solvedProblemRepository.save(
            createSolvedProblem(testMember, problem, "답변입니다", "피드백입니다", SaveStatus.SAVED)
        );

        // when & then
        mockMvc.perform(get("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value("면접문제 풀이 여부 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.isSolved").value(true));
    }

    @Test
    @DisplayName("면접문제 풀이 여부 조회 - 사용자가 문제를 풀지 않은 경우 false 반환")
    void checkInterviewProblemSolved_false() throws Exception {
        // given
        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("문제2", ProblemType.DEVOPS)
        );

        // when & then
        mockMvc.perform(get("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value("면접문제 풀이 여부 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.isSolved").value(false));
    }

    @Test
    @DisplayName("면접 문제 저장 - 성공")
    void saveInterviewProblem_success() throws Exception {
        // given
        Member member = memberRepository.save(createMember("interviewUser", "user@test.com", 99L));
        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("백엔드 문제입니다.", ProblemType.BACKEND)
        );
        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);
        solvedProblemRepository.save(
            createSolvedProblem(member, problem, "answer", "feedback", SaveStatus.UNSAVED));

        // when & then
        mockMvc.perform(post("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("면접문제 저장에 성공하였습니다."));
    }

    @Test
    @DisplayName("면접 문제 저장 - 문제 풀이 이력이 없는 경우 예외 발생")
    void saveInterviewProblem_notSolvedYet() throws Exception {
        // given
        Member member = memberRepository.save(
            createMember("interviewUser", "user@test.com", 99L));

        InterviewProblem unsolvedProblem = interviewProblemRepository.save(
            createInterviewProblem("AI 문제입니다.", ProblemType.AI));

        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        // when & then
        mockMvc.perform(
                post("/api/v1/members/interview-problems/{problemId}", unsolvedProblem.getId())
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SOLVED_INTERVIEW_PROBLEM_NOT_EXIST.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.SOLVED_INTERVIEW_PROBLEM_NOT_EXIST.getMessage()));
    }

    @Test
    @DisplayName("면접 문제 저장 - 이미 저장한 문제일 경우 예외 발생")
    void saveInterviewProblem_alreadySaved() throws Exception {
        // given
        Member member = memberRepository.save(
            createMember("interviewUser", "user@test.com", 99L));

        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("프론트 문제", ProblemType.FRONTEND));

        solvedProblemRepository.save(
            createSolvedProblem(member, problem, "answer", "feedback", SaveStatus.SAVED)
        );

        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        // when & then
        mockMvc.perform(post("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.INTERVIEW_PROBLEM_ALREADY_SAVED.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.INTERVIEW_PROBLEM_ALREADY_SAVED.getMessage()));
    }

    @Test
    @DisplayName("면접 문제 저장 취소 - 성공")
    void cancelSaveInterviewProblem_success() throws Exception {
        // given
        Member member = memberRepository.save(createMember("testNick", "user@bee.com", 1L));
        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("백엔드 문제", ProblemType.BACKEND));
        solvedProblemRepository.save(
            createSolvedProblem(member, problem, "answer", "feedback", SaveStatus.SAVED));

        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        // when & then
        mockMvc.perform(patch("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("면접문제 저장 취소에 성공하였습니다."));
    }

    @Test
    @DisplayName("면접 문제 저장 취소 - 문제를 푼 기록이 없을 경우 예외")
    void cancelSaveInterviewProblem_notSolved() throws Exception {
        // given
        Member member = memberRepository.save(createMember("tester", "test@naver.com", 2L));
        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("AI 문제", ProblemType.AI));

        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        // when & then
        mockMvc.perform(patch("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SOLVED_INTERVIEW_PROBLEM_NOT_EXIST.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value(
                CustomResponseStatus.SOLVED_INTERVIEW_PROBLEM_NOT_EXIST.getMessage()));
    }

    @Test
    @DisplayName("면접 문제 저장 취소 - 이미 취소된 경우 예외")
    void cancelSaveInterviewProblem_alreadyCanceled() throws Exception {
        // given
        Member member = memberRepository.save(createMember("tester", "cancel@test.com", 3L));
        InterviewProblem problem = interviewProblemRepository.save(
            createInterviewProblem("DevOps 문제", ProblemType.DEVOPS));
        solvedProblemRepository.save(
            createSolvedProblem(member, problem, "answer", "feedback", SaveStatus.UNSAVED));

        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        // when & then
        mockMvc.perform(patch("/api/v1/members/interview-problems/{problemId}", problem.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.INTERVIEW_PROBLEM_ALREADY_UNSAVED.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value(
                CustomResponseStatus.INTERVIEW_PROBLEM_ALREADY_UNSAVED.getMessage()));
    }
}
