package org.choon.careerbee.domain.company.dto.response;

import java.util.List;

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
  public record Financials(
      Integer annualSalary,
      Integer startingSalary,
      Integer revenue,
      Integer operatingProfit
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