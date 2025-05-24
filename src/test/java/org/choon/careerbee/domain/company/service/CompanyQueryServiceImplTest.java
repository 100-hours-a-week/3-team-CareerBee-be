package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyQueryServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private WishCompanyRepository wishCompanyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CompanyQueryServiceImpl companyQueryService;

    @Test
    @DisplayName("정상 주소와 조건으로 회사 조회 시 레포지토리 호출 및 결과 반환")
    void fetchCompaniesByDistance_shouldCallRepository_andReturnExpectedResult() {
        // given
        CompanyQueryAddressInfo addressInfo = new CompanyQueryAddressInfo(
            37.40024430415324, 127.10698761648364
        );
        CompanyQueryCond queryCond = new CompanyQueryCond(500);
        CompanyRangeSearchResp expectedResponse = new CompanyRangeSearchResp(List.of());

        when(companyRepository.fetchByDistanceAndCondition(addressInfo, queryCond))
            .thenReturn(expectedResponse);

        // when
        CompanyRangeSearchResp actualResponse = companyQueryService.fetchCompaniesByDistance(
            addressInfo, queryCond);

        // then
        verify(companyRepository, times(1)).fetchByDistanceAndCondition(addressInfo, queryCond);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

}