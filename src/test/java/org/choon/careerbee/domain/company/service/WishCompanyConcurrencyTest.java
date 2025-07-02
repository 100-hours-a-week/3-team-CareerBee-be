package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.WishCompanyFixture.createWishCompany;

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
import org.choon.careerbee.domain.company.service.command.CompanyCommandService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WishCompanyConcurrencyTest {

    private static final String COMPANY_WISH_KEY_PREFIX = "company:wish:";

    @Autowired
    private WishCompanyRepository wishCompanyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyCommandService companyCommandService;

    @Autowired
    private RedissonClient redissonClient;

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

    @Test
    @DisplayName("100개의 스레드에서 동시에 관심 등록 요청 시, wishCount가 정확히 100 증가한다")
    void registWishCompany_concurrencyTest() throws InterruptedException {
        // given
        for (long i = 1; i <= 100; i++) {
            memberRepository.save(
                createMember("user" + i, "test" + i + "@test.com", i)
            );
        }
        companyRepository.save(createCompany("testCompany", 37.1, 127.1));

        final int threadCount = 100;
        final Long companyId = 1L;

        String wishCountKey = "company:wish:" + companyId;
        RAtomicLong initialCounter = redissonClient.getAtomicLong(wishCountKey);
        initialCounter.set(0);

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (long i = 1; i <= threadCount; i++) {
            final Long memberId = i;

            executorService.submit(() -> {
                try {
                    companyCommandService.registWishCompany(memberId, companyId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long finalWishCount = redissonClient.getAtomicLong(wishCountKey).get();
        assertThat(finalWishCount).isEqualTo(threadCount);

        // DB의 실제 count도 확인하여 교차 검증
        long dbWishCount = wishCompanyRepository.fetchWishCountById(companyId);
        assertThat(dbWishCount).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("100명의 다른 사용자가 동시에 관심 취소 요청 시, wishCount가 정확히 0으로 감소한다")
    void deleteWishCompany_concurrency_shouldDecrementCounterAtomically()
        throws InterruptedException {
        // given
        final int threadCount = 100;
        Company company = companyRepository.save(createCompany("테스트 기업", 37.0, 127.0));
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member member = memberRepository.save(
                createMember("user" + i, "user" + i + "@test.com", (long) i));
            members.add(member);
            // DB에 관심 등록 기록 저장
            wishCompanyRepository.save(createWishCompany(company, member));
        }

        String wishCountKey = COMPANY_WISH_KEY_PREFIX + company.getId();
        RAtomicLong counter = redissonClient.getAtomicLong(wishCountKey);
        counter.set(threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (Member member : members) {
            executorService.submit(() -> {
                try {
                    companyCommandService.deleteWishCompany(member.getId(), company.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long finalWishCount = redissonClient.getAtomicLong(wishCountKey).get();
        long dbWishCount = wishCompanyRepository.fetchWishCountById(company.getId());

        assertThat(finalWishCount).isEqualTo(0);
        assertThat(dbWishCount).isEqualTo(0);
    }
}
