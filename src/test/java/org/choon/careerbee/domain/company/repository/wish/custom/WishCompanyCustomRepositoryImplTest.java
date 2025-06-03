package org.choon.careerbee.domain.company.repository.wish.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.WishCompanyFixture.createWishCompany;

import jakarta.transaction.Transactional;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.entity.Company;
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
class WishCompanyCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private WishCompanyCustomRepositoryImpl wishCompanyCustomRepository;

    @Test
    @DisplayName("회원으로 관심 기업 ID 목록 조회 - 정상 조회")
    void fetchWishCompanyIdsByMember_shouldReturnCompanyIds() {
        // given
        Member member = createMember("tester", "test@careerbee.com", 1L);
        em.persist(member);

        Company company1 = createCompany("관심기업1", 37.123, 127.123);
        Company company2 = createCompany("관심기업2", 37.124, 127.124);
        Company company3 = createCompany("관심기업3", 37.125, 127.125);

        em.persist(company1);
        em.persist(company2);
        em.persist(company3);

        em.persist(createWishCompany(company1, member));
        em.persist(createWishCompany(company2, member));
        em.persist(createWishCompany(company3, member));

        em.flush();
        em.clear();

        // when
        WishCompanyIdResp result = wishCompanyCustomRepository.fetchWishCompanyIdsByMember(member);

        // then
        assertThat(result).isNotNull();
        assertThat(result.wishCompanies()).containsExactlyInAnyOrder(
            company1.getId(), company2.getId(), company3.getId()
        );
    }

    @Test
    @DisplayName("회원으로 관심 기업 ID 목록 조회 - 관심 기업이 없다면 빈 리스트 반환")
    void fetchWishCompanyIdsByMember_shouldReturnEmptyList_NoWishCompany() {
        // given
        Member member = createMember("tester", "test@careerbee.com", 1L);
        em.persist(member);

        Company company1 = createCompany("관심기업1", 37.123, 127.123);
        Company company2 = createCompany("관심기업2", 37.124, 127.124);
        Company company3 = createCompany("관심기업3", 37.125, 127.125);

        em.persist(company1);
        em.persist(company2);
        em.persist(company3);

        em.flush();
        em.clear();

        // when
        WishCompanyIdResp result = wishCompanyCustomRepository.fetchWishCompanyIdsByMember(member);

        // then
        assertThat(result).isNotNull();
        assertThat(result.wishCompanies()).isEmpty();
    }

    @Test
    @DisplayName("회원의 관심 기업 목록 조회 - 페이징 포함 정상 조회")
    void fetchWishCompaniesByMemberId_shouldReturnPagedResult() {
        // given
        Member member = createMember("tester", "test@careerbee.com", 1L);
        em.persist(member);

        Company company1 = createCompany("관심기업1", 37.123, 127.123);
        Company company2 = createCompany("관심기업2", 37.124, 127.124);
        Company company3 = createCompany("관심기업3", 37.125, 127.125);

        em.persist(company1);
        em.persist(company2);
        em.persist(company3);

        em.persist(createWishCompany(company1, member));
        em.persist(createWishCompany(company2, member));
        em.persist(createWishCompany(company3, member));

        em.flush();
        em.clear();

        // when
        int size = 2;
        Long cursor = null;
        var result = wishCompanyCustomRepository.fetchWishCompaniesByMemberId(member.getId(),
            cursor, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.wishCompanies()).hasSizeLessThanOrEqualTo(size);
        assertThat(result.hasNext()).isTrue();

        // 다음 페이지 요청
        Long nextCursor = result.nextCursor();
        var nextResult = wishCompanyCustomRepository.fetchWishCompaniesByMemberId(member.getId(),
            nextCursor, size);

        assertThat(nextResult.wishCompanies()).hasSizeLessThanOrEqualTo(size);
        assertThat(nextResult.hasNext()).isFalse();
    }
}
