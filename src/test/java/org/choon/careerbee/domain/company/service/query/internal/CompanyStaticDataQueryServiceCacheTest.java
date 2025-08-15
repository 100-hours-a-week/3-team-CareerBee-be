package org.choon.careerbee.domain.company.service.query.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
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
class CompanyStaticDataQueryServiceCacheTest {

    @Autowired
    private CompanyStaticDataQueryService companyStaticDataQueryService;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("companyStaticDetail").clear();
    }

    @Test
    @DisplayName("[회사 정적 데이터 조회] 첫 호출에는 DB를 조회하고, 두 번째 호출에는 캐시에서 결과를 반환한다")
    void fetchCompanyStaticPart_shouldCacheResult() {
        // given
        Long companyId = 1L;
        CompanyStaticPart expected = new CompanyStaticPart(
            companyId,
            "넥슨코리아",
            "testTitle",
            "https://example.com/logo.png",
            CompanyType.MID_SIZED,
            "경기 성남시 분당구",
            3000,
            "https://company.nexon.com/",
            "국내 대표 게임 개발사",
            4.5,
            new CompanyStaticPart.Financials(6000, 4000, 1000000000L, 200000000L),
            List.of(new CompanyStaticPart.Photo(1, "https://example.com/photo1.png")),
            List.of(new CompanyStaticPart.Benefit("복지", "자유복장, 점심 제공")),
            List.of(new CompanyStaticPart.TechStack(1L, "Spring", "BACKEND",
                "https://example.com/spring.png"))
        );

        when(companyRepository.fetchCompanyStaticInfoById(companyId)).thenReturn(expected);

        // when
        CompanyStaticPart result1 = companyStaticDataQueryService.fetchCompanyStaticPart(companyId);

        // then
        verify(companyRepository, times(1)).fetchCompanyStaticInfoById(companyId);
        assertThat(result1).isEqualTo(expected);

        // when
        CompanyStaticPart result2 = companyStaticDataQueryService.fetchCompanyStaticPart(companyId);

        // then
        verify(companyRepository, times(1)).fetchCompanyStaticInfoById(companyId); // 캐시 적용
        assertThat(result2).isEqualTo(expected);
    }
}
