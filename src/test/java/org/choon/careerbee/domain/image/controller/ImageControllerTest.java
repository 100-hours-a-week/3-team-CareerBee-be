package org.choon.careerbee.domain.image.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.image.dto.request.PresignedUrlReq;
import org.choon.careerbee.domain.image.enums.SupportedExtension;
import org.choon.careerbee.domain.image.enums.UploadType;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
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
class ImageControllerTest {

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
        testMember = memberRepository.saveAndFlush(
            MemberFixture.createMember("nick", "nick@a.com", 1L));
        accessToken = "Bearer " + jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("presigned URL 생성 - 프로필 이미지 요청 성공")
    void generatePresignedUrl_profileImage_success() throws Exception {
        // given
        PresignedUrlReq req = new PresignedUrlReq(
            "test.jpg", SupportedExtension.JPG, UploadType.PROFILE_IMAGE
        );

        // when & then
        mockMvc.perform(post("/api/v1/s3/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("presignedUrl 생성에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.data.uploadUrl").isString())
            .andExpect(jsonPath("$.data.objectKey").isString());
    }

    @Test
    @DisplayName("presigned URL 생성 - 이력서에 JPG 확장자 요청시 예외")
    void generatePresignedUrl_resumeWithInvalidExtension() throws Exception {
        // given
        PresignedUrlReq req = new PresignedUrlReq(
            "resume.jpg", SupportedExtension.JPG, UploadType.RESUME
        );

        // when & then
        mockMvc.perform(post("/api/v1/s3/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.EXTENSION_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.EXTENSION_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("presigned URL 생성 - 프로필 이미지에 PDF 확장자 요청시 예외")
    void generatePresignedUrl_profileWithPdfExtension() throws Exception {
        // given
        PresignedUrlReq req = new PresignedUrlReq(
            "profile.pdf", SupportedExtension.PDF, UploadType.PROFILE_IMAGE
        );

        // when & then
        mockMvc.perform(post("/api/v1/s3/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.EXTENSION_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.EXTENSION_NOT_EXIST.getHttpStatusCode()));
    }
}
