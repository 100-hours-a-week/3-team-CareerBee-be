package org.choon.careerbee.domain.company.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.choon.careerbee.domain.company.entity.enums.BenefitType;

public record CompanyDetailResp(
    Long id,
    String name,
    String title,
    String logoUrl,
    String recentIssue,
    String companyType,
    String recruitingStatus,
    String address,
    Integer employeeCount,
    String homepageUrl,
    String description,
    Integer wishCount,
    Double rating,
    Financials financials,
    List<Photo> photos,
    List<Benefit> benefits,
    List<TechStack> techStacks,
    List<Recruitment> recruitments
) {

  public static List<CompanyDetailResp.Benefit> convertBenefitMap(Map<String, List<String>> benefitMap) {
    if (benefitMap == null || benefitMap.isEmpty()) return List.of();

    Map<String, List<String>> grouped = new HashMap<>();

    for (Map.Entry<String, List<String>> entry : benefitMap.entrySet()) {
      String type = BenefitType.fromLabel(entry.getKey());

      grouped.computeIfAbsent(type, k -> new ArrayList<>()).addAll(entry.getValue());
    }

    return grouped.entrySet().stream()
        .map(e -> new CompanyDetailResp.Benefit(e.getKey(), String.join(", ", e.getValue())))
        .toList();
  }

  public record Financials(
      Integer annualSalary,
      Integer startingSalary,
      Long revenue,
      Long operatingProfit
  ) {}

  public record Photo(
      Integer order,
      String url
  ) {}

  public record Benefit(
      String type,
      String description
  ) {}

  public record TechStack(
      Long id,
      String name,
      String type,
      String imgUrl
  ) {}

  public record Recruitment(
      Long id,
      String url,
      String title,
      String startDate,
      String endDate
  ) {}
}