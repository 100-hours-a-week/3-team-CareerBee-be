package org.choon.careerbee.domain.company.repository.custom;

import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;

public interface CompanyCustomRepository {

  CompanyRangeSearchResp fetchByDistanceAndCondition(CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond);
}
