package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WishCompanyConcurrencyTest {

    @Autowired
    private WishCompanyRepository wishCompanyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyCommandService companyCommandService;

    @Test
    @DisplayName("Redisson 기반 중복 요청 방지 - 하나만 성공하고 나머지는 CustomException 발생")
    void registerWishWithRedissonDeduplication_shouldPreventDuplicates()
        throws InterruptedException {
        // given
        Member member = memberRepository.save(createMember("user1", "test@test.com", 123L));
        Company company = companyRepository.save(createCompany("company1", 37.1, 127.1));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    companyCommandService.registWishCompany(member.getId(), company.getId());
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        long count = wishCompanyRepository.count();

        assertThat(count).isEqualTo(1);
        assertThat(exceptions.size()).isEqualTo(threadCount - 1);
        assertThat(exceptions.get(0)).isInstanceOf(CustomException.class);
    }
}
