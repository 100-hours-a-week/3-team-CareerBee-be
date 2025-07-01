package org.choon.careerbee.domain.company.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.WishCompanyFixture.createWishCompany;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    @DisplayName("주어진 위도 경도를 기준으로 반경 내 기업이 정상적으로 조회되는가")
    void fetchByDistanceAndCondition_shouldReturnOnlyCompaniesWithinGivenRadius() {
        // given
        double lat = 37.40024430415324;
        double lon = 127.10698761648364;

        // 1km 이내 기업 3개
        em.persist(createCompany("테스트 기업1", lat, lon)); // 기준점
        em.persist(createCompany("테스트 기업2", lat + 0.005, lon)); // 약 555m 북쪽
        em.persist(createCompany("테스트 기업3", lat, lon + 0.005)); // 약 450m 동쪽

        // 1km 밖 기업들
        em.persist(createCompany("테스트 기업4", lat + 0.02, lon)); // 약 2.2km 북쪽
        em.persist(createCompany("테스트 기업5", lat, lon + 0.02)); // 약 1.7km 동쪽
        em.persist(createCompany("테스트 기업6", lat - 0.015, lon - 0.015)); // 남서쪽 약 2km

        em.flush();
        em.clear();

        CompanyQueryAddressInfo addressInfo = new CompanyQueryAddressInfo(37.40024430415324,
            127.10698761648364);
        CompanyQueryCond cond = new CompanyQueryCond(1000, RecruitingStatus.ONGOING,
            BusinessType.PLATFORM);

        // when
        CompanyRangeSearchResp result = companyCustomRepository.fetchByDistanceAndCondition(
            addressInfo, cond);

        // then
        assertThat(result.companies()).hasSize(3);
    }

    @Test
    @DisplayName("존재하는 company id로 기업 간단 정보 조회시 정상 조회")
    void fetchCompanySummaryById_shouldReturnCompanySummaryResp() {
        // given
        Company company = createCompany("테스트 회사", 37.40203443, 127.1034665);
        em.persist(company);

        Member member = createMember("testNickname", "test@test.com", 1L);
        em.persist(member);

        WishCompany wishCompany = createWishCompany(company, member);
        em.persist(wishCompany);

        em.flush();
        em.clear();

        CompanySummaryInfo expectedResp = new CompanySummaryInfo(
            company.getId(),
            company.getName(),
            company.getLogoUrl(),
            1L,
            List.of()
        );

        // when
        CompanySummaryInfo actualResp = companyCustomRepository.fetchCompanySummaryInfoById(
            company.getId());

        // then
        assertThat(actualResp).isNotNull();
        assertThat(actualResp.id()).isEqualTo(expectedResp.id());
        assertThat(actualResp.name()).isEqualTo(expectedResp.name());
        assertThat(actualResp.wishCount()).isEqualTo(expectedResp.wishCount());
        assertThat(actualResp.logoUrl()).isEqualTo(expectedResp.logoUrl());
        assertThat(actualResp.keywords()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 id로 간단 정보 조회시 404 예외 발생")
    void fetchCompanySummaryById_shouldThrowException_whenCompanyNotFound() {
        // given
        Company comp = createCompany("테스트기업", 37.123, 127.34);
        em.persist(comp);
        em.flush();
        em.clear();

        Long invalidCompanyId = comp.getId() + 100L;

        // when & then
        assertThatThrownBy(
            () -> companyCustomRepository.fetchCompanySummaryInfoById(invalidCompanyId))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("존재하는 기업의 위치정보 조회시 정상 조회 ")
    void fetchCompanyMarkerInfo_shouldReturnMarkerInfo() {
        // given
        Company company = createCompany(
            "마커 테스트 회사", 37.40203443, 127.1034665
        );
        em.persist(company);
        em.flush();
        em.clear();

        // when
        CompanyMarkerInfo actualResult = companyCustomRepository.fetchCompanyMarkerInfo(
            company.getId());

        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.id()).isEqualTo(company.getId());
        assertThat(actualResult.businessType()).isEqualTo(company.getBusinessType());
        assertThat(actualResult.recruitingStatus()).isEqualTo(company.getRecruitingStatus());
        assertThat(actualResult.locationInfo().longitude()).isCloseTo(127.1034665, within(1e-9));
        assertThat(actualResult.locationInfo().latitude()).isCloseTo(37.40203443, within(1e-9));
    }

    @Test
    @DisplayName("존재하지 않는 기업의 위치정보 조회하면 404 예외 발생")
    void fetchCompanyMarkerInfo_shouldReturn404_whenNonExistCompany() {
        // given
        Company company = createCompany(
            "마커 테스트 회사", 37.40203443, 127.1034665
        );
        em.persist(company);
        em.flush();
        em.clear();

        Long nonExistCompanyId = company.getId() + 100L;

        // when & then
        assertThatThrownBy(() -> companyCustomRepository.fetchCompanyMarkerInfo(nonExistCompanyId))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage());
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

        int searchMaxCount = 8;

        // when
        CompanySearchResp actualResp = companyCustomRepository.fetchMatchingCompaniesByKeyword(
            "테스트");

        // then
        assertThat(actualResp.matchingCompanies().size()).isEqualTo(searchMaxCount);
    }

    @Test
    @DisplayName("기업 정적 정보 조회 - 성공")
    void fetchCompanyStaticInfoById_shouldReturnStaticPart() {
        // given
        Company company = createCompany("정적 회사", 37.1, 127.1);
        em.persist(company);

        // when
        CompanyStaticPart result = companyCustomRepository.fetchCompanyStaticInfoById(
            company.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(company.getId());
        assertThat(result.name()).isEqualTo(company.getName());
    }

    @Test
    @DisplayName("기업 최근 이슈 조회 - 성공")
    void fetchCompanyRecentIssueById_shouldReturnRecentIssue() {
        // given
        Company company = createCompany("이슈 기업", 37.2, 127.2);
        em.persist(company);

        // when
        String recentIssue = companyCustomRepository.fetchCompanyRecentIssueById(company.getId());

        // then
        assertThat(recentIssue).isEqualTo(company.getRecentIssue());
    }

    @Test
    @DisplayName("기업 채용 정보 조회 - 성공")
    void fetchRecruitmentInfo_shouldReturnRecruitments() {
        // given
        Company company = createCompany("채용 기업", 37.5, 127.5);
        Recruitment recruit = Recruitment.from(
            company, 12343L, "test.url", "title",
            LocalDateTime.of(2025, 6, 10, 12, 0, 0),
            LocalDateTime.of(2025, 6, 19, 12, 0, 0)
        );
        em.persist(company);
        em.persist(recruit);

        // when
        CompanyRecruitInfo info = companyCustomRepository.fetchRecruitmentInfo(company.getId());

        // then
        assertThat(info.recruitments()).hasSize(1);
        assertThat(info.recruitments().get(0).title()).isEqualTo("title");
    }

}
