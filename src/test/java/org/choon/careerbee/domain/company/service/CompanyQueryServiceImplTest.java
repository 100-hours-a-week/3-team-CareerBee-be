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
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
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

    @Test
    @DisplayName("기업 상세 조회 시 repository 호출 및 결과 반환")
    void fetchCompanyDetail_ShouldReturnDetailResponse() {
        // given
        Long companyId = 1L;
        CompanyDetailResp.Financials financials = new CompanyDetailResp.Financials(6000, 4000, 1000000000L, 200000000L);
        List<CompanyDetailResp.Photo> photos = List.of(new CompanyDetailResp.Photo(1, "https://example.com/photo1.png"));
        List<CompanyDetailResp.Benefit> benefits = List.of(new CompanyDetailResp.Benefit("복지", "자유복장, 점심 제공"));
        List<CompanyDetailResp.TechStack> techStacks = List.of(new CompanyDetailResp.TechStack(1L, "Spring", "BACKEND", "https://example.com/spring.png"));
        List<CompanyDetailResp.Recruitment> recruitments = List.of(new CompanyDetailResp.Recruitment(1L, "https://jobs.com/1", "백엔드 개발자", "2024-01-01", "2024-12-31"));

        CompanyDetailResp expectedResponse = new CompanyDetailResp(
            companyId,
            "넥슨코리아",
            "백엔드 개발자",
            "https://example.com/logo.png",
            "최근 이슈 설명",
            "대기업",
            "채용중",
            "경기 성남시 분당구",
            3000,
            "https://company.nexon.com/",
            "국내 대표 게임 개발사",
            123,
            4.5,
            financials,
            photos,
            benefits,
            techStacks,
            recruitments
        );

        when(companyRepository.fetchCompanyDetailById(companyId)).thenReturn(expectedResponse);

        // when
        CompanyDetailResp actualResponse = companyQueryService.fetchCompanyDetail(companyId);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(companyRepository, times(1)).fetchCompanyDetailById(companyId);
    }

}