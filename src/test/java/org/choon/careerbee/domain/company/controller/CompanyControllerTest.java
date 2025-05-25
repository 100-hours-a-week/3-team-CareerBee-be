package org.choon.careerbee.domain.company.controller;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.HamcrestCondition;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("유효하지 않은 id인 경우 404 에러 발생")
    void fetchCompanySummary_shouldReturn404_whenCompanyNotFound() throws Exception {
        Long invalidCompanyId = 1000L;

        mockMvc.perform(get("/api/v1/companies/{companyId}/summary", invalidCompanyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(CustomResponseStatus.COMPANY_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("기업 상세 정보 조회 API가 성공적으로 데이터를 반환한다")
    void fetchCompanyDetail_success() throws Exception {
        Company company = createCompany("통합테스트기업", 37.40203443, 127.1034665);
        em.persist(company);
        em.flush();
        em.clear();
        Long companyId = company.getId();

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

    @Test
    @DisplayName("유효하지 않은 id인 경우 404 에러 발생")
    void fetchCompanyDetail_shouldReturn404_whenCompanyNotFound() throws Exception {
        Long invalidCompanyId = -999L;

        mockMvc.perform(get("/api/v1/companies/{companyId}", invalidCompanyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(CustomResponseStatus.COMPANY_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("기업 위치 정보 조회 API가 성공적으로 데이터를 반환한다")
    void fetchCompanyLocationInfo_success() throws Exception {
        Company company = createCompany("마커 테스트 기업", 37.40203443, 127.1034665);
        em.persist(company);
        em.flush();
        em.clear();
        Long companyId = company.getId();

        mockMvc.perform(get("/api/v1/companies/{companyId}/locations", companyId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("기업 위치 정보 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.id").value(company.getId()))
            .andExpect(jsonPath("$.data.businessType").value(company.getBusinessType().toString()))
            .andExpect(jsonPath("$.data.recruitingStatus").value(company.getRecruitingStatus().toString()))
            .andExpect(jsonPath("$.data.locationInfo.latitude").value(closeTo(37.40203443, 1e-9)))
            .andExpect(jsonPath("$.data.locationInfo.longitude").value(closeTo(127.1034665, 1e-9)));
    }

    private Company createCompany(String name, double latitude, double longitude) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(longitude, latitude));
        return Company.builder()
            .name(name)
            .geoPoint(geoPoint)
            .address("서울시 강남구")
            .homeUrl("https://company.test.com")
            .logoUrl("https://logo.test.com")
            .description("테스트 기업 설명")
            .recentIssue("테스트 이슈")
            .companyType(CompanyType.MID_SIZED)
            .recruitingStatus(RecruitingStatus.ONGOING)
            .businessType(BusinessType.PLATFORM)
            .score(100)
            .employeeCount(50)
            .annualSalary(60000)
            .startingSalary(40000)
            .revenue(1000000000L)
            .operatingProfit(200000000L)
            .ir("IR 정보")
            .rating(4.5)
            .benefits(null)
            .build();
    }
}