package org.choon.careerbee.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyQueryServiceImpl implements CompanyQueryService {
  private final CompanyRepository companyRepository;

  @Override
  public CompanyRangeSearchResp fetchCompaniesByDistance(
      CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond
  ) {
    return companyRepository.fetchByDistanceAndCondition(companyQueryAddressInfo, companyQueryCond);
  }
}
