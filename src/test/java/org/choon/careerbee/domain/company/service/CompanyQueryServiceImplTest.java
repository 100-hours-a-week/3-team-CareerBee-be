package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.LocationInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        CompanyRangeSearchResp expectedResponse = new CompanyRangeSearchResp(List.of(
            new CompanyMarkerInfo(1L, "test.url", BusinessType.PLATFORM, RecruitingStatus.ONGOING,
                new LocationInfo(37.4, 127.3))
        ));

        when(companyRepository.fetchByDistanceAndCondition(addressInfo, queryCond))
            .thenReturn(expectedResponse);

        // when
        CompanyRangeSearchResp actualResponse = companyQueryService.fetchCompaniesByDistance(
            addressInfo, queryCond);

        // then
        ArgumentCaptor<CompanyQueryAddressInfo> addressCaptor = ArgumentCaptor.forClass(
            CompanyQueryAddressInfo.class);
        ArgumentCaptor<CompanyQueryCond> condCaptor = ArgumentCaptor.forClass(
            CompanyQueryCond.class);
        verify(companyRepository, times(1))
            .fetchByDistanceAndCondition(addressCaptor.capture(), condCaptor.capture());
        assertThat(addressCaptor.getValue()).isEqualTo(addressInfo);
        assertThat(condCaptor.getValue()).isEqualTo(queryCond);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.companies()).hasSize(1);

        CompanyMarkerInfo firstMarker = actualResponse.companies().get(0);
        assertThat(firstMarker.id()).isEqualTo(1L);
        assertThat(firstMarker.markerUrl()).isEqualTo("test.url");
        assertThat(firstMarker.businessType()).isEqualTo(BusinessType.PLATFORM);
        assertThat(firstMarker.recruitingStatus()).isEqualTo(RecruitingStatus.ONGOING);
        assertThat(firstMarker.locationInfo().latitude()).isEqualTo(37.4);
        assertThat(firstMarker.locationInfo().longitude()).isEqualTo(127.3);
    }

    @Test
    @DisplayName("기업 간단 조회 시 repository 호출 및 결과 반환")
    void fetchCompanySummary_ShouldReturnSummaryResponse() {
        // given
        Long companyId = 1L;
        CompanySummaryInfo expectedResponse = new CompanySummaryInfo(
            companyId,
            "테스트 회사",
            "https://test.logo.jpg",
            1L,
            List.of()
        );
        when(companyRepository.fetchCompanySummaryInfoById(anyLong())).thenReturn(expectedResponse);

        // when
        CompanySummaryInfo actualResponse = companyQueryService.fetchCompanySummary(companyId);

        // then
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(companyRepository, times(1)).fetchCompanySummaryInfoById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(companyId);
        assertThat(actualResponse.name()).isEqualTo(expectedResponse.name());
        assertThat(actualResponse.id()).isEqualTo(expectedResponse.id());
        assertThat(actualResponse.logoUrl()).isEqualTo(expectedResponse.logoUrl());
        assertThat(actualResponse.keywords()).isEqualTo(expectedResponse.keywords());
    }

    @Test
    @DisplayName("기업 상세 조회 시 repository 호출 및 결과 반환")
    void fetchCompanyDetail_ShouldReturnDetailResponse() {
        // given
        Long companyId = 1L;
        CompanyDetailResp.Financials financials = new CompanyDetailResp.Financials(6000, 4000,
            1000000000L, 200000000L);
        List<CompanyDetailResp.Photo> photos = List.of(
            new CompanyDetailResp.Photo(1, "https://example.com/photo1.png"));
        List<CompanyDetailResp.Benefit> benefits = List.of(
            new CompanyDetailResp.Benefit("복지", "자유복장, 점심 제공"));
        List<CompanyDetailResp.TechStack> techStacks = List.of(
            new CompanyDetailResp.TechStack(1L, "Spring", "BACKEND",
                "https://example.com/spring.png"));
        List<CompanyDetailResp.Recruitment> recruitments = List.of(
            new CompanyDetailResp.Recruitment(1L, "https://jobs.com/1", "백엔드 개발자", "2024-01-01",
                "2024-12-31"));

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
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(companyRepository, times(1)).fetchCompanyDetailById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(companyId);
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.id()).isEqualTo(expectedResponse.id());
        assertThat(actualResponse.companyType()).isEqualTo(expectedResponse.companyType());
        assertThat(actualResponse.address()).isEqualTo(expectedResponse.address());
        assertThat(actualResponse.description()).isEqualTo(expectedResponse.description());
        assertThat(actualResponse.employeeCount()).isEqualTo(expectedResponse.employeeCount());
        assertThat(actualResponse.wishCount()).isEqualTo(expectedResponse.wishCount());
        assertThat(actualResponse.rating()).isEqualTo(expectedResponse.rating());
        assertThat(actualResponse.homepageUrl()).isEqualTo(expectedResponse.homepageUrl());
        assertThat(actualResponse.techStacks()).isEqualTo(expectedResponse.techStacks());
    }

    @Test
    @DisplayName("기업 마커 정보 조회 시 repository 호출 및 결과 반환")
    void fetchCompanyLocation_ShouldReturnMarkerInfo() {
        // given
        Long companyId = 1L;
        CompanyMarkerInfo expectedResponse = new CompanyMarkerInfo(
            companyId,
            "testUrl",
            BusinessType.GAME,
            RecruitingStatus.ONGOING,
            new LocationInfo(37.123, 127.01)
        );
        when(companyRepository.fetchCompanyMarkerInfo(companyId)).thenReturn(expectedResponse);

        // when
        CompanyMarkerInfo actualResponse = companyQueryService.fetchCompanyLocation(companyId);

        // then
        verify(companyRepository, times(1)).fetchCompanyMarkerInfo(companyId);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

}
