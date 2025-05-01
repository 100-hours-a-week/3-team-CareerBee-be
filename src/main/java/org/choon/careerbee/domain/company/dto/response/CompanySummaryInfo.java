package org.choon.careerbee.domain.company.dto.response;

import java.util.List;

public record CompanySummaryInfo(
    Long id,
    String name,
    String logoUrl,
    List<Keyword> keywords
) {
  public record Keyword(
      String content
  ) {}
}
