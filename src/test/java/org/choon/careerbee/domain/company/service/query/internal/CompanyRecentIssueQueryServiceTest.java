package org.choon.careerbee.domain.company.service.query.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CompanyRecentIssueQueryServiceCacheTest {

    @Autowired
    private CompanyRecentIssueQueryService companyRecentIssueQueryService;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("recentIssue").clear();
    }

    @Test
    @DisplayName("[기업 최근 이슈 조회] 첫 호출에는 DB를 조회하고, 두 번째 호출에는 캐시에서 결과를 반환한다")
    void fetchRecentIssue_shouldCacheResult() {
        // given
        Long companyId = 1L;
        String expectedIssue = "최근 AI 전략 발표";

        when(companyRepository.fetchCompanyRecentIssueById(companyId)).thenReturn(expectedIssue);

        // when
        String result1 = companyRecentIssueQueryService.fetchRecentIssue(companyId);

        // then
        verify(companyRepository, times(1)).fetchCompanyRecentIssueById(companyId);
        assertThat(result1).isEqualTo(expectedIssue);

        // when
        String result2 = companyRecentIssueQueryService.fetchRecentIssue(companyId);

        // then
        verify(companyRepository, times(1)).fetchCompanyRecentIssueById(companyId);
        assertThat(result2).isEqualTo(expectedIssue);
    }
}
