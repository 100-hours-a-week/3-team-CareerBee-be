package org.choon.careerbee.domain.company.dto.response;

import java.util.List;

public record CompanyRangeSearchResp(
    List<CompanySummary> companies
) {

  public record CompanySummary(
      Long id,
      String logoUrl,
      LocationInfo locationInfo
  ) {}

  public record LocationInfo(
      double latitude,
      double longitude
  ) {}
}