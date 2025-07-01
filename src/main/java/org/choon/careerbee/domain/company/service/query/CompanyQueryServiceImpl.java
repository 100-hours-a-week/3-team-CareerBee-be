package org.choon.careerbee.domain.company.service.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.dto.internal.CompanySummaryInfoWithoutWish;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CheckWishCompanyResp;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.company.service.query.internal.CompanyRecentIssueQueryService;
import org.choon.careerbee.domain.company.service.query.internal.CompanyRecruitmentQueryService;
import org.choon.careerbee.domain.company.service.query.internal.CompanyStaticDataQueryService;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyQueryServiceImpl implements CompanyQueryService {

    private static final String COMPANY_SIMPLE_KEY_PREFIX = "company:simple:";
    private static final String COMPANY_WISH_KEY_PREFIX = "company:wish:";
    private static final Long COMPANY_WISH_KEY_TTL = 10L;

    private final CompanyRepository companyRepository;
    private final WishCompanyRepository wishCompanyRepository;
    private final MemberRepository memberRepository;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private final CompanyRecruitmentQueryService recruitmentQueryService;
    private final CompanyStaticDataQueryService staticDataQueryService;
    private final CompanyRecentIssueQueryService recentIssueQueryService;

    @Override
    public CompanyRangeSearchResp fetchCompaniesByDistance(
        CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond
    ) {
        return companyRepository.fetchByDistanceAndCondition(
            companyQueryAddressInfo,
            companyQueryCond
        );
    }

    @Override
    public CompanySummaryInfo fetchCompanySummary(Long companyId) {
        try {
            CompanySummaryInfoWithoutWish simpleInfo = fetchSimpleInfo(companyId);
            Long wishCount = fetchWishCount(companyId);

            return new CompanySummaryInfo(
                simpleInfo.id(),
                simpleInfo.name(),
                simpleInfo.logoUrl(),
                wishCount,
                simpleInfo.keywords()
            );
        } catch (JsonProcessingException e) {
            throw new CustomException(CustomResponseStatus.JSON_PARSING_ERROR);
        }
    }

    @Override
    public CompanyDetailResp fetchCompanyDetail(Long companyId) {
        CompanyStaticPart companyStaticPart = staticDataQueryService
            .fetchCompanyStaticPart(companyId);
        CompanyRecruitInfo companyRecruitInfo = recruitmentQueryService
            .fetchRecruitmentInfo(companyId);
        String companyRecentIssue = recentIssueQueryService
            .fetchRecentIssue(companyStaticPart.id());
        Long wishCount = fetchWishCount(companyId);

        return CompanyDetailResp.of(
            companyStaticPart,
            companyRecentIssue,
            wishCount,
            companyRecruitInfo
        );
    }

    @Override
    public CheckWishCompanyResp checkWishCompany(Long accessMemberId, Long companyId) {
        Member validMember = memberRepository.findById(accessMemberId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        Company validCompany = companyRepository.findById(companyId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST));

        return new CheckWishCompanyResp(
            wishCompanyRepository.existsByMemberAndCompany(validMember, validCompany));
    }

    @Override
    public CompanySearchResp fetchMatchingCompaniesByKeyword(String keyword) {
        return companyRepository.fetchMatchingCompaniesByKeyword(escapeLike(keyword));
    }

    @Override
    public WishCompanyIdResp fetchWishCompanyIds(Long accessMemberId) {
        Member validMember = memberRepository.findById(accessMemberId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        return wishCompanyRepository.fetchWishCompanyIdsByMember(validMember);
    }

    @Override
    @Cacheable(cacheNames = "companyMarkerInfo", key = "#companyId")
    public CompanyMarkerInfo fetchCompanyLocation(Long companyId) {
        return companyRepository.fetchCompanyMarkerInfo(companyId);
    }

    @Override
    public Company findById(Long companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST));
    }

    @Override
    public List<Company> findBySaraminNameIn(List<String> companyNames) {
        return companyRepository.findBySaraminNameIn(companyNames);
    }

    @Override
    public WishCompaniesResp fetchWishCompanies(Long id, Long cursor, int size) {
        return wishCompanyRepository.fetchWishCompaniesByMemberId(id, cursor, size);
    }

    @Override
    public List<CompanyMarkerInfo> fetchAllCompanyLocations() {
        return companyRepository.fetchAllCompanyMarkerInfo();
    }

    private String escapeLike(String keyword) {
        return keyword.strip()
            .chars()
            .mapToObj(c -> {
                char ch = (char) c;
                return (ch == '!' || ch == '_' || ch == '%') ? "!" + ch : String.valueOf(ch);
            })
            .collect(Collectors.joining());
    }

    private CompanySummaryInfoWithoutWish fetchSimpleInfo(Long companyId)
        throws JsonProcessingException {
        RBucket<String> simpleInfoBucket = redissonClient.getBucket(
            COMPANY_SIMPLE_KEY_PREFIX + companyId
        );

        String simpleJson = simpleInfoBucket.get();

        if (simpleJson != null) {
            return objectMapper.readValue(simpleJson, CompanySummaryInfoWithoutWish.class);
        }

        CompanySummaryInfoWithoutWish simpleInfo =
            companyRepository.fetchCompanySummaryInfoWithoutWishCount(companyId);
        simpleInfoBucket.set(objectMapper.writeValueAsString(simpleInfo));
        return simpleInfo;
    }

    private Long fetchWishCount(Long companyId) {
        RBucket<String> wishCountBucket = redissonClient.getBucket(
            COMPANY_WISH_KEY_PREFIX + companyId
        );

        String wishCountJson = wishCountBucket.get();

        if (wishCountJson != null) {
            return Long.valueOf(wishCountJson);
        }

        Long wishCount = wishCompanyRepository.fetchWishCountById(companyId);
        wishCountBucket.set(String.valueOf(wishCount), Duration.ofSeconds(COMPANY_WISH_KEY_TTL));
        return wishCount;
    }
}
