package org.choon.careerbee.domain.company.repository.custom;

import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;

public interface CompanyCustomRepository {

    CompanyRangeSearchResp fetchByDistanceAndCondition(
        CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond);

    CompanySummaryInfo fetchCompanySummaryInfoById(Long companyId);

    CompanyDetailResp fetchCompanyDetailById(Long companyId);

    CompanySearchResp fetchMatchingCompaniesByKeyword(String keyword);

    CompanyMarkerInfo fetchCompanyMarkerInfo(Long companyId);

    WishCompaniesResp fetchWishCompaniesByMemberId(Long memberId, Long cursor, int size);
}
