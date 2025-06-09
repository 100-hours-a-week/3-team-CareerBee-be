package org.choon.careerbee.domain.member.controller;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String accessToken;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(createMember("testnick", "test@test.com", 1L));
        accessToken = jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("이력 정보 수정 - DTO 객체 기반 요청 성공")
    void updateResumeInfo_success() throws Exception {
        // given
        UpdateResumeReq req = new UpdateResumeReq(
            "BR1",
            2,
            3,
            MajorType.MAJOR,
            "Nexon",
            18,
            "백엔드 개발자",
            "메이플스토리 개발"
        );

        String json = objectMapper.writeValueAsString(req);

        // when & then
        mockMvc.perform(patch("/api/v1/members/resume")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("이력 정보 수정이 완료되었습니다."));
    }

    @Test
    @DisplayName("이력 정보 수정 - 존재하지 않는 멤버인 경우 404 예외 반환")
    void updateResumeInfo_memberNotFound_shouldReturn404() throws Exception {
        // given
        Long invalidMemberId = testMember.getId() + 100L;
        String invalidToken = jwtUtil.createToken(invalidMemberId, TokenType.ACCESS_TOKEN);

        UpdateResumeReq req = new UpdateResumeReq(
            "BR1", 1, 1, MajorType.MAJOR, "카카오", 12, "백엔드", "인턴 경험 있음"
        );
        String json = objectMapper.writeValueAsString(req);

        // when & then
        mockMvc.perform(patch("/api/v1/members/resume")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.httpStatusCode").value(404))
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage()));
    }

    @Test
    @DisplayName("내 정보 수정 - 정상 요청 시 204 No Content 반환")
    void updateProfileInfo_success() throws Exception {
        // given
        UpdateProfileInfoReq req = new UpdateProfileInfoReq(
            "https://img.example.com/profile.png",
            "새닉네임",
            "new_email@example.com"
        );
        String json = objectMapper.writeValueAsString(req);

        // when & then
        mockMvc.perform(patch("/api/v1/members")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("내 정보 수정이 완료되었습니다."));
    }

    @Test
    @DisplayName("내 정보 수정 - 인증 없이 요청 시 401 반환")
    void updateProfileInfo_unauthorized_shouldReturn401() throws Exception {
        UpdateProfileInfoReq req = new UpdateProfileInfoReq(
            "https://img.example.com/profile.png", "newNick", "unauth@example.com"
        );
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(patch("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 정보 수정 - 중복 이메일이면 409 Conflict 반환")
    void updateProfileInfo_duplicateEmail_shouldReturn409() throws Exception {
        // given
        memberRepository.saveAndFlush(createMember("testnickname", "duplicate@test.com", 2345L));
        UpdateProfileInfoReq req = new UpdateProfileInfoReq(
            "https://img.example.com/profile.png",
            "new nickname",
            "duplicate@test.com"
        );
        String json = objectMapper.writeValueAsString(req);

        // when & then
        mockMvc.perform(patch("/api/v1/members")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.httpStatusCode").value(409))
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.EMAIL_ALREADY_EXIST.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴 - 정상 요청 시 204 응답")
    void withdrawal_success() throws Exception {
        WithdrawalReq req = new WithdrawalReq("서비스 이용 안함");
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(delete("/api/v1/members")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 - 존재하지 않는 회원이면 404 예외 반환")
    void withdrawal_memberNotFound_shouldReturn404() throws Exception {
        // given
        Long invalidId = testMember.getId() + 999L;
        String invalidToken = jwtUtil.createToken(invalidId, TokenType.ACCESS_TOKEN);

        WithdrawalReq req = new WithdrawalReq("사용자 없음");
        String json = objectMapper.writeValueAsString(req);

        // when & then
        mockMvc.perform(delete("/api/v1/members")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.httpStatusCode").value(404))
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage()));
    }

}
