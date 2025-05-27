package org.choon.careerbee.domain.company.controller;

import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.company.entity.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("기업 간단 정보 조회 API가 성공적으로 데이터를 반환한다")
    void fetchCompanySummary_success() throws Exception {
        Company company = createCompany("통합테스트기업", 37.40203443, 127.1034665);
        em.persist(company);
        em.flush();
        em.clear();
        Long companyId = company.getId();

        mockMvc.perform(get("/api/v1/companies/{companyId}/summary", companyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("기업 간단 정보 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.id").value(company.getId()))
            .andExpect(jsonPath("$.data.name").value(company.getName()))
            .andExpect(jsonPath("$.data.logoUrl").value(company.getLogoUrl()))
            .andExpect(jsonPath("$.data.wishCount").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 회사 ID로 조회 시 404 에러가 발생한다.")
    void fetchCompanySummary_shouldReturn404_whenCompanyNotFound() throws Exception {
        // given
        for (int i = 0; i < 100; i++) {
            em.persist(createCompany("test" + i, 37.123, 127.23));
        }
        em.flush();
        em.clear();
        Long nonExistCompanyId = 1000L;

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}/summary", nonExistCompanyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.COMPANY_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("기업 상세 정보 조회 API가 성공적으로 데이터를 반환한다")
    void fetchCompanyDetail_success() throws Exception {
        // given
        Company company = createCompany("통합테스트기업", 37.40203443, 127.1034665);
        em.persist(company);
        em.flush();
        em.clear();
        Long companyId = company.getId();

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("기업 상세 정보 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.id").value(company.getId()))
            .andExpect(jsonPath("$.data.name").value(company.getName()))
            .andExpect(jsonPath("$.data.logoUrl").value(company.getLogoUrl()))
            .andExpect(jsonPath("$.data.rating").value(company.getRating()))
            .andExpect(jsonPath("$.data.financials.annualSalary").value(company.getAnnualSalary()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", " "})
    @DisplayName("상세조회시 유효하지 않은 입력의 경우 400 에러가 발생한다.")
    void fetchCompanyDetail_shouldReturn400_whenInvalidInput(String invalidInput) throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", invalidInput)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.INVALID_INPUT_VALUE.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.INVALID_INPUT_VALUE.getHttpStatusCode()));
    }

    @Test
    @DisplayName("상세조회시 존재하지 않는 회사 ID 조회시 404 에러가 발생한다.")
    void fetchCompanyDetail_shouldReturn404_whenNonExistCompany() throws Exception {
        // given
        Company company = createCompany("통합테스트기업", 37.40203443, 127.1034665);
        em.persist(company);
        em.flush();
        em.clear();

        Long nonExistCompanyId = 2L;

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", nonExistCompanyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.COMPANY_NOT_EXIST.getHttpStatusCode()));
    }
}
