package org.choon.careerbee.domain.company.service;

import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CheckWishCompanyResp;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;

public interface CompanyQueryService {

  CompanyRangeSearchResp fetchCompaniesByDistance(CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond);

  CompanySummaryInfo fetchCompanySummary(Long companyId);

  CheckWishCompanyResp checkWishCompany(Long accessMemberId, Long companyId);

  CompanyDetailResp fetchCompanyDetail(Long companyId);
}
