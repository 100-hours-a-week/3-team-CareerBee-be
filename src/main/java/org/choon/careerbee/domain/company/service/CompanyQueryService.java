package org.choon.careerbee.domain.company.service;

import java.util.List;
import java.util.Optional;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CheckWishCompanyResp;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.dto.response.WishCompanyProgressResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;

public interface CompanyQueryService {

    CompanyRangeSearchResp fetchCompaniesByDistance(CompanyQueryAddressInfo companyQueryAddressInfo,
        CompanyQueryCond companyQueryCond);

    CompanySummaryInfo fetchCompanySummary(Long companyId);

    CheckWishCompanyResp checkWishCompany(Long accessMemberId, Long companyId);

    CompanyDetailResp fetchCompanyDetail(Long companyId);

    CompanySearchResp fetchMatchingCompaniesByKeyword(String keyword);

    WishCompanyIdResp fetchWishCompanyIds(Long accessMemberId);

    CompanyMarkerInfo fetchCompanyLocation(Long companyId);

    Company findById(Long companyId);

    Optional<Company> findBySaraminName(String name);

    List<Company> findBySaraminNameIn(List<String> companyNames);

    WishCompaniesResp fetchWishCompanies(Long id, Long cursor, int size);

    WishCompanyProgressResp fetchWishCompanyProgress(Long wishCompanyId, Long accessMemberId);
}
