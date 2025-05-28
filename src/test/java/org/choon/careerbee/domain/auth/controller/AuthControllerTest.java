package org.choon.careerbee.domain.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("소셜 로그인 URL 요청이 성공적으로 처리된다")
    void getOAuthLoginUrl_success() throws Exception {
        // given
        String origin = "http://localhost:5173";
        String expectedLoginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=1342353523423&redirect_uri=https://test.co.kr";

        // when & then
        mockMvc.perform(get("/api/v1/auth/oauth")
                .param("type", "kakao")
                .header("Origin", origin)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("소셜 로그인 url 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.data.loginUrl").value(expectedLoginUrl));
    }

    @Test
    @DisplayName("소셜 로그인 URL 요청 시 Origin 헤더 누락 시 400 예외 발생")
    void getOAuthLoginUrl_shouldReturn400_whenOriginHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth")
                .param("type", "kakao")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.INVALID_INPUT_VALUE.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.INVALID_INPUT_VALUE.getHttpStatusCode()));
    }


}
