package org.choon.careerbee.domain.company.service.query.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
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
class CompanyRecruitmentQueryServiceCacheTest {

    @Autowired
    private CompanyRecruitmentQueryService companyRecruitmentQueryService;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("recruitments").clear();
    }

    @Test
    @DisplayName("첫 호출에는 DB를 조회하고, 두 번째 호출에는 캐시에서 결과를 반환한다")
    void fetchRecruitmentInfo_shouldCacheResult() {
        // given
        Long companyId = 1L;
        CompanyRecruitInfo expected = new CompanyRecruitInfo(
            RecruitingStatus.ONGOING,
            List.of(
                new CompanyRecruitInfo.Recruitment(
                    1L, "https://jobs.com/1", "백엔드 개발자", "2024-01-01", "2024-12-31"
                )
            )
        );

        when(companyRepository.fetchRecruitmentInfo(companyId)).thenReturn(expected);

        // when
        CompanyRecruitInfo result1 = companyRecruitmentQueryService.fetchRecruitmentInfo(companyId);

        // then
        verify(companyRepository, times(1)).fetchRecruitmentInfo(companyId);
        assertThat(result1).isEqualTo(expected);

        // when
        CompanyRecruitInfo result2 = companyRecruitmentQueryService.fetchRecruitmentInfo(companyId);

        // then
        verify(companyRepository, times(1))
            .fetchRecruitmentInfo(companyId);
        assertThat(result2).isEqualTo(expected);
    }
}
