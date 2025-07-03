package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo.Recruitment;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.dto.internal.CompanySummaryInfoWithoutWish;
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
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo.Keyword;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.company.service.query.CompanyQueryServiceImpl;
import org.choon.careerbee.domain.company.service.query.internal.CompanyRecentIssueQueryService;
import org.choon.careerbee.domain.company.service.query.internal.CompanyRecruitmentQueryService;
import org.choon.careerbee.domain.company.service.query.internal.CompanyStaticDataQueryService;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CompanyQueryServiceImplTest {

    private static final String GEO_KEY_PREFIX = "company:markerInfo:";
    private static final String COMPANY_SIMPLE_KEY_PREFIX = "company:simple:";
    private static final String COMPANY_WISH_KEY_PREFIX = "company:wish:";
    private static final Long COMPANY_WISH_KEY_TTL = 10L;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private WishCompanyRepository wishCompanyRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedissonClient redissonClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CompanyQueryServiceImpl companyQueryService;

    @Mock
    private CompanyRecruitmentQueryService recruitmentQueryService;

    @Mock
    private CompanyRecentIssueQueryService recentIssueQueryService;

    @Mock
    private CompanyStaticDataQueryService staticDataQueryService;

    @Test
    @DisplayName("정상 주소와 조건으로 회사 조회 시 레포지토리 호출 및 결과 반환")
    void fetchCompaniesByDistance_shouldCallRepository_andReturnExpectedResult() {
        // given
        CompanyQueryAddressInfo addressInfo = new CompanyQueryAddressInfo(
            37.40024430415324, 127.10698761648364
        );
        CompanyQueryCond queryCond = new CompanyQueryCond(500, RecruitingStatus.CLOSED,
            BusinessType.PLATFORM);
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
    @DisplayName("[기업 위치 정보 조회] Repository에 조회를 위임하고 결과를 반환한다")
    void fetchCompanyLocation_delegatesToRepository() {
        // given
        Long companyId = 1L;
        CompanyMarkerInfo expectedInfo = new CompanyMarkerInfo(
            companyId, "test.jpg", BusinessType.COMMERCE, RecruitingStatus.CLOSED,
            new LocationInfo(37.123, 127.12)
        );

        // Repository가 특정 정보를 반환하도록 설정
        when(companyRepository.fetchCompanyMarkerInfo(companyId)).thenReturn(expectedInfo);

        // when
        CompanyMarkerInfo result = companyQueryService.fetchCompanyLocation(companyId);

        // then
        // 1. 반환된 결과가 기대와 같은지 확인
        assertThat(result).isEqualTo(expectedInfo);

        // 2. Repository의 메서드가 정확히 1번 호출되었는지 확인
        verify(companyRepository, times(1)).fetchCompanyMarkerInfo(companyId);
    }

    @Test
    @DisplayName("기업 간단 정보 데이터가 캐시에 모두 존재하는 경우")
    void fetchCompanySummary_allCacheHit_returnsFromCache() throws Exception {
        // given
        Long companyId = 1L;
        CompanySummaryInfoWithoutWish simpleInfo = new CompanySummaryInfoWithoutWish(
            companyId, "테스트회사", "https://logo.png", List.of(new Keyword("AI"))
        );
        String simpleJson = objectMapper.writeValueAsString(simpleInfo);
        String wishCountJson = "10";

        RBucket<String> simpleBucket = mock(RBucket.class);
        RBucket<String> wishBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(COMPANY_SIMPLE_KEY_PREFIX + companyId)).thenReturn(
            simpleBucket);
        when(redissonClient.<String>getBucket(COMPANY_WISH_KEY_PREFIX + companyId)).thenReturn(
            wishBucket);
        when(simpleBucket.get()).thenReturn(simpleJson);
        when(wishBucket.get()).thenReturn(wishCountJson);
        when(objectMapper.readValue(simpleJson, CompanySummaryInfoWithoutWish.class)).thenReturn(
            simpleInfo);

        // when
        CompanySummaryInfo result = companyQueryService.fetchCompanySummary(companyId);

        // then
        verify(companyRepository, never()).fetchCompanySummaryInfoWithoutWishCount(any());
        verify(wishCompanyRepository, never()).fetchWishCountById(any());
        assertThat(result.wishCount()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("기업 간단 정보 - 캐시가 존재하지 않는 경우 DB 조회 및 캐시 저장 후 반환")
    void fetchCompanySummary_allCacheMiss_shouldQueryDbAndCache() throws Exception {
        // given
        Long companyId = 2L;
        CompanySummaryInfoWithoutWish simpleInfo = new CompanySummaryInfoWithoutWish(
            companyId, "새로운회사", "https://newlogo.png", List.of(new Keyword("Data"))
        );
        String simpleJson = objectMapper.writeValueAsString(simpleInfo);
        Long wishCount = 42L;
        String wishCountJson = String.valueOf(wishCount);

        RBucket<String> simpleBucket = mock(RBucket.class);
        RBucket<String> wishBucket = mock(RBucket.class);

        when(redissonClient.<String>getBucket(COMPANY_SIMPLE_KEY_PREFIX + companyId)).thenReturn(
            simpleBucket);
        when(redissonClient.<String>getBucket(COMPANY_WISH_KEY_PREFIX + companyId)).thenReturn(
            wishBucket);

        when(simpleBucket.get()).thenReturn(null);
        when(wishBucket.get()).thenReturn(null);

        when(companyRepository.fetchCompanySummaryInfoWithoutWishCount(companyId)).thenReturn(
            simpleInfo);
        when(wishCompanyRepository.fetchWishCountById(companyId)).thenReturn(wishCount);
        when(objectMapper.writeValueAsString(simpleInfo)).thenReturn(simpleJson);

        // when
        CompanySummaryInfo result = companyQueryService.fetchCompanySummary(companyId);

        // then
        verify(companyRepository).fetchCompanySummaryInfoWithoutWishCount(companyId);
        verify(wishCompanyRepository).fetchWishCountById(companyId);
        verify(simpleBucket).set(simpleJson);
        verify(wishBucket).set(wishCountJson, Duration.ofSeconds(COMPANY_WISH_KEY_TTL));
        assertThat(result.wishCount()).isEqualTo(42L);
        assertThat(result.name()).isEqualTo("새로운회사");
    }

    @Test
    @DisplayName("기업 간단 정보 - 단순 정보 캐시만 존재하는 경우 관심수만 DB 조회 후 캐시 저장")
    void fetchCompanySummary_onlySimpleInfoCached_shouldFetchWishCountAndCacheIt()
        throws Exception {
        // given
        Long companyId = 3L;
        CompanySummaryInfoWithoutWish simpleInfo = new CompanySummaryInfoWithoutWish(
            companyId, "부분캐시회사", "https://logo.partial.png", List.of(new Keyword("Cloud"))
        );
        String simpleJson = objectMapper.writeValueAsString(simpleInfo);
        Long wishCount = 20L;

        RBucket<String> simpleBucket = mock(RBucket.class);
        RBucket<String> wishBucket = mock(RBucket.class);

        when(redissonClient.<String>getBucket(COMPANY_SIMPLE_KEY_PREFIX + companyId)).thenReturn(
            simpleBucket);
        when(redissonClient.<String>getBucket(COMPANY_WISH_KEY_PREFIX + companyId)).thenReturn(
            wishBucket);

        when(simpleBucket.get()).thenReturn(simpleJson);
        when(wishBucket.get()).thenReturn(null);

        when(objectMapper.readValue(simpleJson, CompanySummaryInfoWithoutWish.class)).thenReturn(
            simpleInfo);
        when(wishCompanyRepository.fetchWishCountById(companyId)).thenReturn(wishCount);

        // when
        CompanySummaryInfo result = companyQueryService.fetchCompanySummary(companyId);

        // then
        verify(companyRepository, never()).fetchCompanySummaryInfoWithoutWishCount(any());
        verify(wishCompanyRepository).fetchWishCountById(companyId);
        verify(wishBucket).set(String.valueOf(wishCount), Duration.ofSeconds(COMPANY_WISH_KEY_TTL));
        assertThat(result.name()).isEqualTo("부분캐시회사");
        assertThat(result.wishCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("기업 간단 정보 - 단순 정보 캐시 역직렬화 실패 시 예외 발생")
    void fetchCompanySummary_whenSimpleJsonParsingFails_shouldThrowException() throws Exception {
        // given
        Long companyId = 4L;
        String invalidJson = "INVALID_JSON";

        RBucket<String> simpleBucket = mock(RBucket.class);
        RBucket<String> wishBucket = mock(RBucket.class);

        when(redissonClient.<String>getBucket(COMPANY_SIMPLE_KEY_PREFIX + companyId)).thenReturn(
            simpleBucket);
        when(simpleBucket.get()).thenReturn(invalidJson);

        // when & then
        assertThatThrownBy(() -> companyQueryService.fetchCompanySummary(companyId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.JSON_PARSING_ERROR.getMessage());
    }

    @Test
    @DisplayName("기업 상세 조회 - 의존 서비스 호출 및 CompanyDetailResp 반환")
    void fetchCompanyDetail_ShouldReturnDetailResponse() {
        // given
        Long companyId = 1L;
        CompanyStaticPart staticPart = new CompanyStaticPart(
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

        CompanyRecruitInfo recruitInfo = new CompanyRecruitInfo(
            RecruitingStatus.ONGOING,
            List.of(
                new Recruitment(1L, "https://jobs.com/1", "백엔드 개발자", "2024-01-01", "2024-12-31"))
        );

        RBucket<String> wishBucket = mock(RBucket.class);
        String recentIssue = "최근 이슈 설명";
        Long wishCount = 123L;

        when(staticDataQueryService.fetchCompanyStaticPart(companyId)).thenReturn(staticPart);
        when(recruitmentQueryService.fetchRecruitmentInfo(companyId)).thenReturn(recruitInfo);
        when(recentIssueQueryService.fetchRecentIssue(companyId)).thenReturn(recentIssue);
        when(redissonClient.<String>getBucket(COMPANY_WISH_KEY_PREFIX + companyId)).thenReturn(
            wishBucket);
        when(wishBucket.get()).thenReturn("123");

        // when
        CompanyDetailResp actual = companyQueryService.fetchCompanyDetail(companyId);

        // then
        assertThat(actual.id()).isEqualTo(staticPart.id());
        assertThat(actual.name()).isEqualTo(staticPart.name());
        assertThat(actual.companyType()).isEqualTo(staticPart.companyType());
        assertThat(actual.address()).isEqualTo(staticPart.address());
        assertThat(actual.homepageUrl()).isEqualTo(staticPart.homepageUrl());
        assertThat(actual.description()).isEqualTo(staticPart.description());
        assertThat(actual.logoUrl()).isEqualTo(staticPart.logoUrl());
        assertThat(actual.employeeCount()).isEqualTo(staticPart.employeeCount());
        assertThat(actual.wishCount()).isEqualTo(wishCount);
        assertThat(actual.rating()).isEqualTo(staticPart.rating());
        assertThat(actual.recruitingStatus()).isEqualTo(recruitInfo.recruitingStatus());
        assertThat(actual.title()).isEqualTo(staticPart.title());
        assertThat(actual.recruitments()).isEqualTo(recruitInfo.recruitments());
        assertThat(actual.recentIssue()).isEqualTo(recentIssue);
        assertThat(actual.financials()).isEqualTo(staticPart.financials());
        assertThat(actual.photos()).isEqualTo(staticPart.photos());
        assertThat(actual.benefits()).isEqualTo(staticPart.benefits());
        assertThat(actual.techStacks()).isEqualTo(staticPart.techStacks());

        verify(staticDataQueryService, times(1)).fetchCompanyStaticPart(companyId);
        verify(recruitmentQueryService, times(1)).fetchRecruitmentInfo(companyId);
        verify(recentIssueQueryService, times(1)).fetchRecentIssue(companyId);
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
        ReflectionTestUtils.setField(mockMember, "id", 1L);
        WishCompanyIdResp mockResp = new WishCompanyIdResp(List.of(10L, 20L, 30L));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(wishCompanyRepository.fetchWishCompanyIdsByMember(memberId)).thenReturn(mockResp);

        // when
        WishCompanyIdResp actualResp = companyQueryService.fetchWishCompanyIds(memberId);

        // then
        assertThat(actualResp.wishCompanies()).containsAll(List.of(10L, 20L, 30L));

        verify(memberRepository, times(1)).findById(memberId);

        ArgumentCaptor<Long> memberIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(wishCompanyRepository, times(1)).fetchWishCompanyIdsByMember(
            memberIdCaptor.capture());
        assertThat(memberIdCaptor.getValue()).isEqualTo(memberId); // 객체 동등성 확인
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

}
