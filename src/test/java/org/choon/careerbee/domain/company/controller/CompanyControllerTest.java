package org.choon.careerbee.domain.company.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
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
            .andExpect(
                jsonPath("$.message").value(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage()))
            .andExpect(jsonPath("$.httpStatusCode").value(
                CustomResponseStatus.COMPANY_NOT_EXIST.getHttpStatusCode()));
    }

    @Test
    @DisplayName("키워드로 기업 검색 시 200 응답과 결과 반환")
    void searchCompanyByKeyword_shouldReturn200() throws Exception {
        // given
        Company kakao = createCompany("카카오", 37.12, 127.13);
        Company kakao_health = createCompany("카카오 헬스케어", 37.12, 127.13);
        Company hyundai = createCompany("현대자동차", 37.12, 127.13);
        Company coupang = createCompany("쿠팡", 37.12, 127.13);

        em.persist(kakao);
        em.persist(kakao_health);
        em.persist(hyundai);
        em.persist(coupang);

        em.flush();
        em.clear();

        // when & then
        mockMvc.perform(get("/api/v1/companies/search")
                .param("keyword", "카")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("매칭 데이터 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.matchingCompanies.length()").value(2))
            .andExpect(jsonPath("$.data.matchingCompanies[0].name").value("카카오"))
            .andExpect(jsonPath("$.data.matchingCompanies[1].name").value("카카오 헬스케어"));
    }

    private Company createCompany(String name, double latitude, double longitude) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(longitude, latitude));
        return Company.builder()
            .name(name)
            .geoPoint(geoPoint)
            .address("서울시 강남구")
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
