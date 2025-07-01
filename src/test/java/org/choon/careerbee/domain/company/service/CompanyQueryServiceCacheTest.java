package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.LocationInfo;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
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
class CompanyQueryServiceCacheTest {

    @Autowired
    private CompanyQueryService companyQueryService;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("companyMarkerInfo").clear();
    }

    @Test
    @DisplayName("[캐싱 테스트] 첫 호출에는 DB를 조회하고, 두 번째 호출에는 캐시에서 결과를 반환한다")
    void fetchCompanyLocation_shouldCacheResult() {
        // given
        Long companyId = 1L;
        CompanyMarkerInfo expectedInfo = new CompanyMarkerInfo(
            companyId, "test.jpg", BusinessType.COMMERCE, RecruitingStatus.CLOSED,
            new LocationInfo(37.123, 127.12)
        );

        when(companyRepository.fetchCompanyMarkerInfo(companyId)).thenReturn(expectedInfo);

        // when: 1. 첫 번째 호출
        CompanyMarkerInfo result1 = companyQueryService.fetchCompanyLocation(companyId);

        // then: 1. DB 조회가 1번 발생했는지 검증
        verify(companyRepository, times(1)).fetchCompanyMarkerInfo(companyId);
        assertThat(result1).isEqualTo(expectedInfo);

        // when: 2. 두 번째 호출 (동일한 인자)
        CompanyMarkerInfo result2 = companyQueryService.fetchCompanyLocation(companyId);

        // then: 2. DB 조회가 추가로 발생하지 않았는지 검증 (캐시 히트)
        // 총 호출 횟수가 여전히 1번이어야 한다.
        verify(companyRepository, times(1)).fetchCompanyMarkerInfo(companyId);
        assertThat(result2).isEqualTo(expectedInfo);
    }
}
