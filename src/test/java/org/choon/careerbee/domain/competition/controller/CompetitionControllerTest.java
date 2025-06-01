package org.choon.careerbee.domain.competition.controller;

import static org.choon.careerbee.fixture.CompetitionProblemFixture.createProblem;
import static org.choon.careerbee.fixture.ProblemChoiceFixture.createProblemChoice;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.domain.problem.CompetitionProblem;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionProblemRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.ProblemChoiceRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.fixture.CompetitionFixture;
import org.choon.careerbee.fixture.MemberFixture;
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
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Autowired
    private CompetitionProblemRepository competitionProblemRepository;

    @Autowired
    private ProblemChoiceRepository problemChoiceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String accessToken;
    private Member testMember;
    private Competition testCompetition;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.saveAndFlush(
            MemberFixture.createMember("nick", "nick@a.com", 1L));
        testCompetition = competitionRepository.saveAndFlush(
            CompetitionFixture.createCompetition(
                LocalDateTime.of(2025, 5, 30, 20, 0, 0),
                LocalDateTime.of(2025, 5, 30, 20, 10, 0)
            )
        );
        accessToken = "Bearer " + jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("대회 참가 - 성공")
    void joinCompetition_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.message").value("대회 입장에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(204));
    }

    @Test
    @DisplayName("대회 참가 - 존재하지 않는 대회")
    void joinCompetition_competitionNotFound() throws Exception {
        // given
        Long nonexistentId = testCompetition.getId() + 100L;

        // when & then
        mockMvc.perform(post("/api/v1/competitions/{competitionId}", nonexistentId)
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage()));
    }

    @Test
    @DisplayName("대회 참가 - 이미 참가한 경우")
    void joinCompetition_alreadyJoined() throws Exception {
        // 첫 번째 참가
        mockMvc.perform(post("/api/v1/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // 두 번째 참가 → 예외
        mockMvc.perform(post("/api/v1/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.COMPETITION_ALREADY_JOIN.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.COMPETITION_ALREADY_JOIN.getMessage()));
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("대회 결과 제출 - 성공")
    void submitCompetitionResult_success() throws Exception {
        // given: 대회에 먼저 참가
        mockMvc.perform(post("/api/v1/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        CompetitionResultSubmitReq request = new CompetitionResultSubmitReq((short) 3, 598);

        // when & then
        mockMvc.perform(
                post("/api/v1/competitions/{competitionId}/results", testCompetition.getId())
                    .header("Authorization", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.message").value("대회 제출에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(204));
    }

    @Test
    @DisplayName("대회 결과 제출 - 이미 제출한 경우")
    void submitCompetitionResult_alreadySubmitted() throws Exception {
        // given: 대회 참가 + 결과 1회 제출
        mockMvc.perform(post("/api/v1/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        CompetitionResultSubmitReq request = new CompetitionResultSubmitReq((short) 2, 420);

        mockMvc.perform(
                post("/api/v1/competitions/{competitionId}/results", testCompetition.getId())
                    .header("Authorization", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        // when & then
        mockMvc.perform(
                post("/api/v1/competitions/{competitionId}/results", testCompetition.getId())
                    .header("Authorization", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.RESULT_ALREADY_SUBMIT.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.RESULT_ALREADY_SUBMIT.getHttpStatusCode()));
    }

    @Test
    @DisplayName("대회 결과 제출 - 존재하지 않는 대회")
    void submitCompetitionResult_competitionNotFound() throws Exception {
        // given
        Long invalidCompetitionId = testCompetition.getId() + 100L;

        CompetitionResultSubmitReq request = new CompetitionResultSubmitReq((short) 3, 123);

        // when & then
        mockMvc.perform(post("/api/v1/competitions/{competitionId}/results", invalidCompetitionId)
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.COMPETITION_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("대회 참여 여부 조회 - 참여한 경우 true 반환")
    void checkCompetitionParticipation_participated() throws Exception {
        // given
        competitionParticipantRepository.save(
            CompetitionParticipant.of(testMember, testCompetition)
        );

        // when & then
        mockMvc.perform(get("/api/v1/members/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isParticipant").value(true))
            .andExpect(jsonPath("$.message").value("대회 참여 여부 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()));
    }

    @Test
    @DisplayName("대회 참여 여부 조회 - 참여하지 않은 경우 false 반환")
    void checkCompetitionParticipation_notParticipated() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/competitions/{competitionId}", testCompetition.getId())
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isParticipant").value(false))
            .andExpect(jsonPath("$.message").value("대회 참여 여부 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()));
    }

    @Test
    @DisplayName("대회 참여 여부 조회 - 존재하지 않는 대회일 경우 404 반환")
    void checkCompetitionParticipation_notExistCompetition() throws Exception {
        // given
        Long invalidCompetitionId = testCompetition.getId() + 100L;

        // when & then
        mockMvc.perform(get("/api/v1/members/competitions/{competitionId}", invalidCompetitionId)
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.COMPETITION_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("대회 문제 조회 - 문제와 보기 정상 조회")
    void fetchCompetitionProblems_success() throws Exception {
        // given: 문제와 보기를 직접 저장
        CompetitionProblem problem = competitionProblemRepository.save(
            createProblem(testCompetition, "문제 제목", "문제 설명", "문제 해설", (short) 2)
        );

        problemChoiceRepository.save(createProblemChoice(problem, "보기 1", (short) 1));
        problemChoiceRepository.save(createProblemChoice(problem, "보기 2", (short) 2));
        problemChoiceRepository.save(createProblemChoice(problem, "보기 3", (short) 3));

        // when & then
        mockMvc.perform(get("/api/v1/competitions/{competitionId}/problems", testCompetition.getId())
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("대회 문제 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.data.problems[0].title").value("문제 제목"))
            .andExpect(jsonPath("$.data.problems[0].choices.length()").value(3))
            .andExpect(jsonPath("$.data.problems[0].choices[1].content").value("보기 2"));
    }

    @Test
    @DisplayName("대회 문제 조회 - 존재하지 않는 대회")
    void fetchCompetitionProblems_competitionNotFound() throws Exception {
        // given
        Long invalidId = testCompetition.getId() + 999L;

        // when & then
        mockMvc.perform(get("/api/v1/competitions/{competitionId}/problems", invalidId)
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(CustomResponseStatus.COMPETITION_NOT_EXIST.getHttpStatusCode()));
    }
}
