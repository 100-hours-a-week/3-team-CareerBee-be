package org.choon.careerbee.domain.competition.controller;

import static org.choon.careerbee.fixture.competition.CompetitionProblemFixture.createProblem;
import static org.choon.careerbee.fixture.competition.CompetitionSummaryFixture.createSummary;
import static org.choon.careerbee.fixture.competition.ProblemChoiceFixture.createProblemChoice;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.domain.problem.CompetitionProblem;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionProblemRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.ProblemChoiceRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.fixture.MemberFixture;
import org.choon.careerbee.fixture.competition.CompetitionFixture;
import org.choon.careerbee.fixture.competition.RankingTestDataSupport;
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
    private EntityManager em;

    @Autowired
    private JwtUtil jwtUtil;

    private RankingTestDataSupport testDataSupport;
    private String accessToken;
    private Member testMember;
    private Competition testCompetition;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.saveAndFlush(
            MemberFixture.createMember("nick", "nick@a.com", 5L));
        testCompetition = competitionRepository.saveAndFlush(
            CompetitionFixture.createCompetition(
                LocalDateTime.of(2025, 5, 30, 20, 0, 0),
                LocalDateTime.of(2025, 5, 30, 20, 10, 0)
            )
        );
        accessToken = "Bearer " + jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);

        testDataSupport = new RankingTestDataSupport(em);
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
        mockMvc.perform(
                get("/api/v1/competitions/{competitionId}/problems", testCompetition.getId())
                    .header("Authorization", accessToken)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("대회 문제 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SUCCESS.getHttpStatusCode()))
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
            .andExpect(jsonPath("$.message").value(
                CustomResponseStatus.COMPETITION_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.COMPETITION_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("랭킹 조회 - 실제 데이터로 성공")
    void fetchCompetitionRankings_success() throws Exception {
        // given
        testDataSupport.prepareRankingData(LocalDate.of(2025, 6, 2));

        // when & then
        mockMvc.perform(get("/api/v1/competitions/rankings")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                .value("랭킹조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.daily.length()").value(2))
            .andExpect(jsonPath("$.data.week.length()").value(2))
            .andExpect(jsonPath("$.data.month.length()").value(3))
            .andExpect(jsonPath("$.data.week[0].continuous").value(2))
            .andExpect(jsonPath("$.data.month[2].nickname").value("member2"));
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 성공")
    void fetchTodayCompetitionId_success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        Competition todayCompetition = competitionRepository.saveAndFlush(
            CompetitionFixture.createCompetition(
                now.withHour(10).withMinute(0),
                now.withHour(23).withMinute(0)
            )
        );

        // when & then
        mockMvc.perform(get("/api/v1/competitions/ids")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value("오늘 대회 id 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.competitionId").value(todayCompetition.getId()));
    }

    @Test
    @DisplayName("오늘 날짜 기준 대회 ID 조회 - 대회 없을 경우 null 반환")
    void fetchTodayCompetitionId_noCompetition() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/competitions/ids")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message").value("오늘 대회 id 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("내 랭킹 조회 - 성공")
    void fetchMemberCompetitionRanking_success() throws Exception {
        // given
        LocalDate today = LocalDate.of(2025, 6, 2);
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        em.persist(createSummary(testMember, (short) 4, 1000L, 1L, SummaryType.DAY, today, today));
        em.persist(
            createSummary(testMember, (short) 8, 2000L, 1L, SummaryType.WEEK, weekStart, weekEnd));
        em.persist(createSummary(testMember, (short) 10, 3000L, 1L, SummaryType.MONTH, monthStart,
            monthEnd));

        em.flush();
        em.clear();

        // when & then
        mockMvc.perform(get("/api/v1/members/competitions/rankings")
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("내 랭킹 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.day").value(1))
            .andExpect(jsonPath("$.data.week").value(1))
            .andExpect(jsonPath("$.data.month").value(1));
    }
}
