package org.choon.careerbee.domain.company.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.transaction.Transactional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDSLConfig.class)
@ActiveProfiles("test")
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CompanyCustomRepositoryImpl companyCustomRepository;

    @Test
    @DisplayName("반경 내 기업 3개가 정상적으로 조회되는가")
    void fetchByDistanceAndCondition_반경내_여러기업조회() {
        // given
        CompanyQueryAddressInfo addressInfo = new CompanyQueryAddressInfo(37.40024430415324,
            127.10698761648364);
        CompanyQueryCond cond = new CompanyQueryCond(1000); // 500m

        // when
        CompanyRangeSearchResp result = companyCustomRepository.fetchByDistanceAndCondition(
            addressInfo, cond);

        // then
        assertThat(result.companies()).hasSize(3);
    }

    @Test
    @DisplayName("유효한 company id로 기업 상세정보 조회시 정상 조회")
    void fetchCompanyDetailById_shouldReturnCompanyDetailResp() {
        // given
        Company company = createCompany(
            "테스트 회사", 37.40203443, 127.1034665
        );
        em.persist(company);
        em.flush();
        em.clear();

        // when
        CompanyDetailResp actualResp = companyCustomRepository.fetchCompanyDetailById(
            company.getId());

        // then
        assertThat(actualResp).isNotNull();
        assertThat(actualResp.name()).isEqualTo(company.getName());
        assertThat(actualResp.address()).isEqualTo(company.getAddress());
        assertThat(actualResp.financials().annualSalary()).isEqualTo(company.getAnnualSalary());
        assertThat(actualResp.rating()).isEqualTo(company.getRating());
        assertThat(actualResp.benefits()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 id로 조회시 예외 발생")
    void fetchCompanyDetailById_shouldThrowException_whenCompanyNotFound() {
        // given
        Long invalidCompanyId = 1000L;

        // when & then
        assertThatThrownBy(() ->
            companyCustomRepository.fetchCompanyDetailById(invalidCompanyId)
        ).hasMessage(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "카, 3",
        "'  카', 0",
        "테스트, 1",
        "존재하지 않는 회사, 0",
    })
    @DisplayName("해당 키워드에 매칭되는 기업에 대해 정상적인 개수 반환")
    void fetchMatchingCompaniesByKeyword_success(String keyword, Integer expectedCount) {
        // given
        Company kakao = createCompany("카카오", 37.123, 127.123);
        Company kakaoHealth = createCompany("카카오 헬스케어", 37.123, 127.123);
        Company testKakao = createCompany("테스트 카카오", 37.123, 127.123);

        em.persist(kakao);
        em.persist(kakaoHealth);
        em.persist(testKakao);
        em.flush();
        em.clear();

        // when
        CompanySearchResp actualResp = companyCustomRepository.fetchMatchingCompaniesByKeyword(
            keyword);

        // then
        assertThat(actualResp.matchingCompanies().size()).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("기업 검색시 최대 8개까지만 조회되는지 확인")
    void fetchMatchingCompaniesByKeyword_shouldReturnAtMost8Companies() {
        // given
        for (int i = 1; i <= 10; i++) {
            Company company = createCompany("테스트 기업 " + i, 37.123 + i * 0.001, 127.123 + i * 0.001);
            em.persist(company);
        }
        em.flush();
        em.clear();

        // when
        CompanySearchResp actualResp = companyCustomRepository.fetchMatchingCompaniesByKeyword(
            "테스트");

        // then
        assertThat(actualResp.matchingCompanies().size()).isEqualTo(8);
    }


    private Company createCompany(
        String name,
        Double latitude,
        Double longitude
    ) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(longitude, latitude));

        return Company.builder()
            .name(name)
            .geoPoint(geoPoint)
            .address("경기 성남시 분당구")
            .logoUrl("https://logo.com")
            .description("회사 소개")
            .companyType(CompanyType.ENTERPRISE)
            .recruitingStatus(RecruitingStatus.ONGOING)
            .businessType(BusinessType.GAME)
            .employeeCount(800)
            .score(300)
            .annualSalary(50000000)
            .startingSalary(0)
            .revenue(662100000000L)
            .operatingProfit(0L)
            .recentIssue("...")
            .rating(4.4)
            .build();
    }
}