package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CheckWishCompanyResp;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.LocationInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp.CompanySearchInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.dto.response.WishCompanyProgressResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(companyRepository, times(1)).fetchCompanyMarkerInfo(captor.capture());
        assertThat(captor.getValue()).isEqualTo(companyId);
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.id()).isEqualTo(expectedResponse.id());
        assertThat(actualResponse.markerUrl()).isEqualTo(expectedResponse.markerUrl());
        assertThat(actualResponse.businessType()).isEqualTo(expectedResponse.businessType());
        assertThat(actualResponse.recruitingStatus()).isEqualTo(
            expectedResponse.recruitingStatus());
        assertThat(actualResponse.locationInfo().latitude()).isEqualTo(
            expectedResponse.locationInfo().latitude());
        assertThat(actualResponse.locationInfo().longitude()).isEqualTo(
            expectedResponse.locationInfo().longitude());
    }

    @Test
    @DisplayName("관심 회사 여부 확인 - 존재하는 경우 true 반환")
    void checkWishCompany_existsTrue() {
        // given
        Long memberId = 1L;
        Long companyId = 1L;
        Member mockMember = createMember("testnick", "test@test.com", 1L);
        Company mockCompany = createCompany("테스트 회사", 37.1234, 127.46);

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));
        when(companyRepository.findById(anyLong())).thenReturn(Optional.of(mockCompany));
        when(wishCompanyRepository.existsByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            true);

        // when
        CheckWishCompanyResp actualResponse =
            companyQueryService.checkWishCompany(memberId, companyId);

        // then
        assertThat(actualResponse.isWish()).isTrue();

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(memberRepository, times(1)).findById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(memberId);

        ArgumentCaptor<Long> captor1 = ArgumentCaptor.forClass(Long.class);
        verify(companyRepository, times(1)).findById(captor1.capture());
        assertThat(captor.getValue()).isEqualTo(companyId);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        verify(wishCompanyRepository)
            .existsByMemberAndCompany(memberCaptor.capture(), companyCaptor.capture());

        assertThat(memberCaptor.getValue()).isEqualTo(mockMember);
        assertThat(companyCaptor.getValue()).isEqualTo(mockCompany);
    }

    @Test
    @DisplayName("관심 회사 여부 확인 - 존재하지 않는 경우 false 반환")
    void checkWishCompany_notExistsFalse() {
        // given
        Long memberId = 1L;
        Long companyId = 1L;
        Member mockMember = createMember("testnick", "test@test.com", 1L);
        Company mockCompany = createCompany("테스트 회사", 37.1234, 127.46);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(mockCompany));
        when(wishCompanyRepository.existsByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            false);

        // when
        CheckWishCompanyResp actualResponse = companyQueryService.checkWishCompany(memberId,
            companyId);

        // then
        assertThat(actualResponse.isWish()).isFalse();

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        verify(wishCompanyRepository)
            .existsByMemberAndCompany(memberCaptor.capture(), companyCaptor.capture());

        assertThat(memberCaptor.getValue()).isEqualTo(mockMember);
        assertThat(companyCaptor.getValue()).isEqualTo(mockCompany);
    }

    @Test
    @DisplayName("관심 회사 여부 확인 - 존재하지 않는 memberId인 경우 예외 발생")
    void checkWishCompany_memberNotExist_throwsException() {
        // given
        Long nonExistMemberId = 1L;
        Long companyId = 1L;

        when(memberRepository.findById(nonExistMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyQueryService.checkWishCompany(nonExistMemberId, companyId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("관심 회사 여부 확인 - 존재하지 않는 companyId인 경우 예외 발생")
    void checkWishCompany_companyNotExist_throwsException() {
        // given
        Long memberId = 1L;
        Long nonExistCompanyId = 1L;
        Member mockMember = createMember("testnick", "test@test.com", 1L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(companyRepository.findById(nonExistCompanyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyQueryService.checkWishCompany(memberId, nonExistCompanyId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.COMPANY_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("기업 검색시 repository 호출 및 결과 반환")
    void fetchMatchingCompaniesByKeyword_shouldReturnResponseFromRepository() {
        // given
        String keyword = "카";
        CompanySearchResp expected = new CompanySearchResp(List.of(
            new CompanySearchInfo(1L, "카카오"),
            new CompanySearchInfo(2L, "카카오 헬스케어")
        ));
        when(companyRepository.fetchMatchingCompaniesByKeyword(anyString())).thenReturn(expected);

        // when
        CompanySearchResp actual = companyQueryService.fetchMatchingCompaniesByKeyword(keyword);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(companyRepository, times(1)).fetchMatchingCompaniesByKeyword(captor.capture());
        assertThat(captor.getValue()).isEqualTo(keyword);

        assertThat(actual).isEqualTo(expected);

        assertThat(actual.matchingCompanies().get(0).id())
            .isEqualTo(expected.matchingCompanies().get(0).id());
        assertThat(actual.matchingCompanies().get(0).name())
            .isEqualTo(expected.matchingCompanies().get(0).name());

        assertThat(actual.matchingCompanies().get(1).id())
            .isEqualTo(expected.matchingCompanies().get(1).id());
        assertThat(actual.matchingCompanies().get(1).name())
            .isEqualTo(expected.matchingCompanies().get(1).name());
    }

    @ParameterizedTest
    @CsvSource({
        "'카카오', 카카오",
        "'  카카오  ', 카카오",
        "'카!오', 카!!오",
        "%, !%",
        "'   ', ''"
    })
    @DisplayName("keyword는 공백 제거 및 escape 처리되어 repository로 전달된다 - 다양한 입력 케이스")
    void fetchMatchingCompaniesByKeyword_shouldTrimAndEscapeKeyword_variants(String rawKeyword,
        String expectedKeyword) {
        // given
        when(companyRepository.fetchMatchingCompaniesByKeyword(anyString())).thenReturn(
            new CompanySearchResp(List.of()));

        // when
        companyQueryService.fetchMatchingCompaniesByKeyword(rawKeyword);

        // then
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(companyRepository).fetchMatchingCompaniesByKeyword(keywordCaptor.capture());
        String actualPassedKeyword = keywordCaptor.getValue();
        assertThat(actualPassedKeyword).isEqualTo(expectedKeyword);
    }

    @Test
    @DisplayName("관심 회사 ID 목록 조회 - 정상 반환")
    void fetchWishCompanyIds_success() {
        // given
        Long memberId = 1L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        WishCompanyIdResp mockResp = new WishCompanyIdResp(List.of(10L, 20L, 30L));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(wishCompanyRepository.fetchWishCompanyIdsByMember(mockMember)).thenReturn(mockResp);

        // when
        WishCompanyIdResp actualResp = companyQueryService.fetchWishCompanyIds(memberId);

        // then
        assertThat(actualResp.wishCompanies()).containsAll(List.of(10L, 20L, 30L));

        verify(memberRepository, times(1)).findById(memberId);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(wishCompanyRepository, times(1)).fetchWishCompanyIdsByMember(memberCaptor.capture());
        assertThat(memberCaptor.getValue()).isEqualTo(mockMember); // 객체 동등성 확인
    }

    @Test
    @DisplayName("관심 회사 ID 목록 조회 - 존재하지 않는 회원일 경우 404 예외 발생")
    void fetchWishCompanyIds_memberNotExist() {
        // given
        Long invalidMemberId = 999L;
        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyQueryService.fetchWishCompanyIds(invalidMemberId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());

        verify(memberRepository, times(1)).findById(invalidMemberId);
        verifyNoInteractions(wishCompanyRepository);
    }

    @Test
    @DisplayName("관심 회사 목록 조회 - repository 호출 및 결과 반환")
    void fetchWishCompanies_shouldCallRepositoryAndReturnResponse() {
        // given
        Long memberId = 1L;
        Long cursor = 100L;
        int size = 10;

        CompanySummaryInfo company1 = new CompanySummaryInfo(
            101L, "회사A", "https://logo.a", 12L, List.of(new CompanySummaryInfo.Keyword("복지"))
        );
        CompanySummaryInfo company2 = new CompanySummaryInfo(
            102L, "회사B", "https://logo.b", 7L, List.of(new CompanySummaryInfo.Keyword("자율출퇴근"))
        );

        WishCompaniesResp mockResp = new WishCompaniesResp(List.of(company1, company2), 200L, true);

        when(wishCompanyRepository.fetchWishCompaniesByMemberId(memberId, cursor, size))
            .thenReturn(mockResp);

        // when
        WishCompaniesResp actual = companyQueryService.fetchWishCompanies(memberId, cursor, size);

        // then
        assertThat(actual).isEqualTo(mockResp);
        assertThat(actual.wishCompanies()).hasSize(2);
        assertThat(actual.nextCursor()).isEqualTo(200L);
        assertThat(actual.hasNext()).isTrue();

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> cursorCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(wishCompanyRepository, times(1))
            .fetchWishCompaniesByMemberId(idCaptor.capture(), cursorCaptor.capture(),
                sizeCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(memberId);
        assertThat(cursorCaptor.getValue()).isEqualTo(cursor);
        assertThat(sizeCaptor.getValue()).isEqualTo(size);
    }

    @Test
    @DisplayName("관심기업 진척도 조회 - 유효한 ID 조합 시 응답 반환")
    void fetchWishCompanyProgress_shouldReturnResponse_whenDataExists() {
        // given
        Long wishCompanyId = 1L;
        Long memberId = 100L;
        WishCompanyProgressResp expected = new WishCompanyProgressResp(80, 320);

        when(wishCompanyRepository.fetchWishCompanyAndMemberProgress(wishCompanyId, memberId))
            .thenReturn(Optional.of(expected));

        // when
        WishCompanyProgressResp actual = companyQueryService.fetchWishCompanyProgress(wishCompanyId,
            memberId);

        // then
        assertThat(actual).isEqualTo(expected);

        verify(wishCompanyRepository, times(1))
            .fetchWishCompanyAndMemberProgress(wishCompanyId, memberId);
    }

    @Test
    @DisplayName("관심기업 진척도 조회 - 존재하지 않는 경우 예외 발생")
    void fetchWishCompanyProgress_shouldThrow_whenWishCompanyNotFound() {
        // given
        Long wishCompanyId = 1L;
        Long memberId = 100L;

        when(wishCompanyRepository.fetchWishCompanyAndMemberProgress(wishCompanyId, memberId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            companyQueryService.fetchWishCompanyProgress(wishCompanyId, memberId)
        )
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.WISH_COMPANY_NOT_FOUND.getMessage());

        verify(wishCompanyRepository, times(1))
            .fetchWishCompanyAndMemberProgress(wishCompanyId, memberId);
    }


}
