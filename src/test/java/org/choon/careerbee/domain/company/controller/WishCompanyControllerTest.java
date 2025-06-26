package org.choon.careerbee.domain.company.controller;

import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.WishCompanyFixture.createWishCompany;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class WishCompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private WishCompanyRepository wishCompanyRepository;

    @Autowired
    private JwtUtil jwtUtil; // 토큰 생성 유틸

    private String jwtToken;

    private Member testMember;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        // given: 회원과 회사 저장
        testMember = memberRepository.save(createMember("testnick", "test@test.com", 1L));
        testCompany = companyRepository.save(createCompany("테스트 회사", 37.1234, 127.46));
        // JWT 토큰 발급
        jwtToken = jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("관심 기업 여부 확인 - 통합 테스트 성공")
    void checkWishCompany_success() throws Exception {
        // given
        wishCompanyRepository.save(createWishCompany(testCompany, testMember));

        // when & then
        mockMvc.perform(get("/api/v1/members/wish-companies/{companyId}", testCompany.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("관심기업 여부 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.isWish").value(true))
            .andExpect(jsonPath("$.httpStatusCode").value(200));
    }

    @Test
    @DisplayName("관심 기업 여부 확인 - 등록 안된 경우 false 반환")
    void checkWishCompany_isWishFalse() throws Exception {
        // given: wishCompany 저장 안함

        // when & then
        mockMvc.perform(get("/api/v1/members/wish-companies/{companyId}", testCompany.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isWish").value(false));
    }

    @Test
    @DisplayName("관심 기업 여부 확인 - 존재하지 않는 회사인 경우 404 예외 발생")
    void checkWishCompany_companyNotFound() throws Exception {
        Long invalidCompanyId = 999L;

        mockMvc.perform(get("/api/v1/members/wish-companies/{companyId}", invalidCompanyId)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.httpStatusCode").value(404))
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "one", "kakao"})
    @DisplayName("관심 기업 여부 확인 - 존재하지 않는 회사인 경우 404 예외 발생")
    void checkWishCompany_invalidCompanyId(String invalidCompanyId) throws Exception {
        mockMvc.perform(get("/api/v1/members/wish-companies/{companyId}", invalidCompanyId)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.httpStatusCode").value(400))
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    @DisplayName("관심 기업 ID 목록 조회 - 관심 기업이 존재할 경우 ID 리스트 반환")
    void fetchWishCompanyIdList_success() throws Exception {
        // given
        Company company1 = companyRepository.saveAndFlush(createCompany("관심기업1", 37.1, 127.1));
        Company company2 = companyRepository.saveAndFlush(createCompany("관심기업2", 37.2, 127.2));
        wishCompanyRepository.saveAndFlush(createWishCompany(company1, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company2, testMember));

        // when & then
        mockMvc.perform(get("/api/v1/members/wish-companies/id-list")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.message").value("관심 기업 아이디 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.wishCompanies").isArray())
            .andExpect(jsonPath("$.data.wishCompanies.length()").value(2))
            .andExpect(jsonPath("$.data.wishCompanies").value(
                org.hamcrest.Matchers.containsInAnyOrder(
                    company1.getId().intValue(), company2.getId().intValue()
                )
            ));
    }

    @Test
    @DisplayName("관심 기업 ID 목록 조회 - 관심 기업이 없을 경우 빈 리스트 반환")
    void fetchWishCompanyIdList_empty() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/wish-companies/id-list")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.data.wishCompanies").isArray())
            .andExpect(jsonPath("$.data.wishCompanies").isEmpty());
    }

    @Test
    @DisplayName("관심 기업 등록 - 성공")
    void registWishCompany_success() throws Exception {
        mockMvc.perform(post("/api/v1/members/wish-companies/{companyId}", testCompany.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("관심기업 등록에 성공하였습니다."));
    }

    @Test
    @DisplayName("관심 기업 등록 - 이미 등록된 경우 예외 반환")
    void registWishCompany_alreadyExists() throws Exception {
        // given
        wishCompanyRepository.saveAndFlush(createWishCompany(testCompany, testMember));

        // when & then
        mockMvc.perform(post("/api/v1/members/wish-companies/{companyId}", testCompany.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.httpStatusCode").value(409))
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.WISH_ALREADY_EXIST.getMessage()));
    }

    @Test
    @DisplayName("관심 기업 삭제 - 성공")
    void deleteWishCompany_success() throws Exception {
        // given
        wishCompanyRepository.saveAndFlush(createWishCompany(testCompany, testMember));

        // when & then
        mockMvc.perform(delete("/api/v1/members/wish-companies/{companyId}", testCompany.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("관심기업 삭제에 성공하였습니다."));
    }

    @Test
    @DisplayName("관심 기업 삭제 - 등록되지 않은 경우 404 예외 발생")
    void deleteWishCompany_notExist_throwsException() throws Exception {
        // given: 등록되지 않은 상태 (wishCompanyRepository 비워둠)

        // when & then
        mockMvc.perform(delete("/api/v1/members/wish-companies/{companyId}", testCompany.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.httpStatusCode").value(404))
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.WISH_COMPANY_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("관심 기업 목록 조회 - 페이징 포함 정상 조회")
    void fetchWishCompanies_success() throws Exception {
        // given: 관심 회사 3개 등록
        Company company1 = companyRepository.saveAndFlush(createCompany("관심기업1", 37.1, 127.1));
        Company company2 = companyRepository.saveAndFlush(createCompany("관심기업2", 37.2, 127.2));
        Company company3 = companyRepository.saveAndFlush(createCompany("관심기업3", 37.3, 127.3));

        wishCompanyRepository.saveAndFlush(createWishCompany(company1, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company2, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company3, testMember));

        // when & then: 첫 페이지
        mockMvc.perform(get("/api/v1/members/wish-companies")
                .header("Authorization", "Bearer " + jwtToken)
                .param("size", "2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.message").value("관심 기업 목록 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.wishCompanies").isArray())
            .andExpect(jsonPath("$.data.wishCompanies.length()").value(2))
            .andExpect(jsonPath("$.data.hasNext").value(true))
            .andExpect(jsonPath("$.data.nextCursor").exists());
    }

    @Test
    @DisplayName("관심 기업 목록 조회 - size 누락시 기본 size(5)로 페이지 조회")
    void fetchWishCompanies_success_defaultPageSize() throws Exception {
        // given: 관심 회사 3개 등록
        int defaultPageSize = 5;

        Company company1 = companyRepository.saveAndFlush(createCompany("관심기업1", 37.1, 127.1));
        Company company2 = companyRepository.saveAndFlush(createCompany("관심기업2", 37.2, 127.2));
        Company company3 = companyRepository.saveAndFlush(createCompany("관심기업3", 37.3, 127.3));
        Company company4 = companyRepository.saveAndFlush(createCompany("관심기업4", 37.3, 127.3));
        Company company5 = companyRepository.saveAndFlush(createCompany("관심기업5", 37.3, 127.3));
        Company company6 = companyRepository.saveAndFlush(createCompany("관심기업6", 37.3, 127.3));

        wishCompanyRepository.saveAndFlush(createWishCompany(company1, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company2, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company3, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company4, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company5, testMember));
        wishCompanyRepository.saveAndFlush(createWishCompany(company6, testMember));

        // when & then
        mockMvc.perform(get("/api/v1/members/wish-companies")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode").value(200))
            .andExpect(jsonPath("$.message").value("관심 기업 목록 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.wishCompanies").isArray())
            .andExpect(jsonPath("$.data.wishCompanies.length()").value(defaultPageSize))
            .andExpect(jsonPath("$.data.hasNext").value(true))
            .andExpect(jsonPath("$.data.nextCursor").exists());
    }

}
