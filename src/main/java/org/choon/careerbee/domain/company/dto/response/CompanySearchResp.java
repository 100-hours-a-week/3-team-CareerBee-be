package org.choon.careerbee.domain.company.dto.response;

import java.util.List;

public record CompanySearchResp(
    List<CompanySearchInfo> matchingCompanies
) {
  public record CompanySearchInfo(
      Long id,
      String name
  ) {}
}
