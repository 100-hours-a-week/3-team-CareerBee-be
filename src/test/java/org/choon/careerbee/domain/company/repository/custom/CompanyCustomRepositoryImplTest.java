package org.choon.careerbee.domain.company.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.choon.careerbee.domain.company.fixture.CompanyFixture.createCompany;

import jakarta.transaction.Transactional;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        CompanyQueryAddressInfo addressInfo = new CompanyQueryAddressInfo(37.40024430415324, 127.10698761648364);
        CompanyQueryCond cond = new CompanyQueryCond(1000);

        // when
        CompanyRangeSearchResp result = companyCustomRepository.fetchByDistanceAndCondition(addressInfo, cond);

        // then
        assertThat(result.companies()).hasSize(3);
    }

    @Test
    @DisplayName("유효한 company id로 기업 간단 정보 조회시 정상 조회")
    void fetchCompanySummaryById_shouldReturnCompanySummaryResp() {
        // given
        Company company = createCompany("테스트 회사", 37.40203443, 127.1034665);
        em.persist(company);

        Member member = createMember();
        em.persist(member);

        WishCompany wishCompany = WishCompany.of(member, company);
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
        CompanySummaryInfo actualResp = companyCustomRepository.fetchCompanySummaryInfoById(company.getId());

        // then
        assertThat(actualResp).isNotNull();
        assertThat(actualResp.id()).isEqualTo(expectedResp.id());
        assertThat(actualResp.name()).isEqualTo(expectedResp.name());
        assertThat(actualResp.wishCount()).isEqualTo(expectedResp.wishCount());
        assertThat(actualResp.logoUrl()).isEqualTo(expectedResp.logoUrl());
        assertThat(actualResp.keywords()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 id로 간단 정보 조회시 예외 발생")
    void fetchCompanySummaryById_shouldThrowException_whenCompanyNotFound() {
        // given
        Long invalidCompanyId = 10000L;

        // when & then
        assertThatThrownBy(() ->
            companyCustomRepository.fetchCompanySummaryInfoById(invalidCompanyId)
        ).hasMessage(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage());
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
        CompanyDetailResp actualResp = companyCustomRepository.fetchCompanyDetailById(company.getId());

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

    private Member createMember() {
        return Member.builder()
            .nickname("tester")
            .email("test@careerbee.com")
            .oAuthProvider(OAuthProvider.KAKAO)
            .providerId(1234235L)
            .build();
    }
}