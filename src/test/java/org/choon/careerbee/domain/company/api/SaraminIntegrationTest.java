package org.choon.careerbee.domain.company.api;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.choon.careerbee.domain.company.exception.RetryableSaraminException;
import org.choon.careerbee.domain.company.service.CompanyCommandService;
import org.choon.careerbee.domain.company.service.RetryStubCompanyCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Import(RetryStubCompanyCommandService.class)
class SaraminIntegrationTest {

    @MockitoBean
    private CompanyApiClient companyApiClient;

    @Autowired
    private CompanyCommandService companyCommandService;

    @Test
    void retryableStub_shouldRetryQuicklyAndRecover() {
        String keyword = "백엔드";
        given(companyApiClient.searchAllRecruitment(keyword))
            .willThrow(new RetryableSaraminException("stub error"));

        companyCommandService.updateCompanyRecruiting(keyword);

        verify(companyApiClient, times(3)).searchAllRecruitment(keyword);
    }
}
