package org.choon.careerbee.domain.company.repository.custom;

import java.util.List;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.dto.internal.CompanySummaryInfoWithoutWish;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.Company;

public interface CompanyCustomRepository {

    CompanyRangeSearchResp fetchByDistanceAndCondition(
        CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond);

    CompanySummaryInfo fetchCompanySummaryInfoById(Long companyId);

    CompanySummaryInfoWithoutWish fetchCompanySummaryInfoWithoutWishCount(Long companyId);

    CompanySearchResp fetchMatchingCompaniesByKeyword(String keyword);

    CompanyMarkerInfo fetchCompanyMarkerInfo(Long companyId);

    List<Company> findBySaraminNameIn(List<String> companyNames);

    List<CompanyMarkerInfo> fetchAllCompanyMarkerInfo();

    CompanyStaticPart fetchCompanyStaticInfoById(Long companyId);

    String fetchCompanyRecentIssueById(Long companyId);

    CompanyRecruitInfo fetchRecruitmentInfo(Long companyId);

}
