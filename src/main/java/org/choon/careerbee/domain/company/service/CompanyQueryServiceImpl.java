package org.choon.careerbee.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
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
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyQueryServiceImpl implements CompanyQueryService {

    private final CompanyRepository companyRepository;
    private final WishCompanyRepository wishCompanyRepository;
    private final MemberRepository memberRepository;

    @Override
    public CompanyRangeSearchResp fetchCompaniesByDistance(
        CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond
    ) {
        return companyRepository.fetchByDistanceAndCondition(companyQueryAddressInfo,
            companyQueryCond);
    }

    @Override
    public CompanySummaryInfo fetchCompanySummary(Long companyId) {
        return companyRepository.fetchCompanySummaryInfoById(companyId);
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
    public CompanyDetailResp fetchCompanyDetail(Long companyId) {
        return companyRepository.fetchCompanyDetailById(companyId);
    }

    @Override
    public CompanySearchResp fetchMatchingCompaniesByKeyword(String keyword) {
        return companyRepository.fetchMatchingCompaniesByKeyword(keyword);
    }

    @Override
    public WishCompanyIdResp fetchWishCompanyIds(Long accessMemberId) {
        Member validMember = memberRepository.findById(accessMemberId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        return wishCompanyRepository.fetchWishCompanyIdsByMember(validMember);
    }

    @Override
    public CompanyMarkerInfo fetchCompanyLocation(Long companyId) {
        return companyRepository.fetchCompanyMarkerInfo(companyId);
    }
}
