package org.choon.careerbee.domain.company.dto.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BenefitType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;

public record CompanyStaticPart(
    Long id,
    String name,
    String title,
    String logoUrl,
    CompanyType companyType,
    String address,
    Integer employeeCount,
    String homepageUrl,
    String description,
    Double rating,
    Financials financials,
    List<Photo> photos,
    List<Benefit> benefits,
    List<TechStack> techStacks
) {

    public record Financials(
        Integer annualSalary,
        Integer startingSalary,
        Long revenue,
        Long operatingProfit
    ) {

    }

    public record Photo(
        Integer order,
        String url
    ) {

    }

    public record Benefit(
        String type,
        String description
    ) {

    }

    public record TechStack(
        Long id,
        String name,
        String type,
        String imgUrl
    ) {

    }

    public static CompanyStaticPart of(
        Company company,
        List<Photo> photos,
        List<TechStack> techStacks
    ) {
        if (company == null) {
            throw new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST);
        }

        return new CompanyStaticPart(
            company.getId(), company.getName(), company.getTitle(),
            company.getLogoUrl(), company.getCompanyType(), company.getAddress(),
            company.getEmployeeCount(), company.getHomeUrl(), company.getDescription(),
            company.getRating(),
            new Financials(
                company.getAnnualSalary(), company.getStartingSalary(),
                company.getRevenue(), company.getOperatingProfit()
            ),
            photos,
            convertBenefitMap(company.getBenefits()),
            techStacks
        );

    }

    private static List<Benefit> convertBenefitMap(
        Map<String, List<String>> benefitMap) {
        if (benefitMap == null || benefitMap.isEmpty()) {
            return List.of();
        }

        Map<String, List<String>> grouped = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : benefitMap.entrySet()) {
            String type = BenefitType.fromLabel(entry.getKey());

            grouped.computeIfAbsent(type, k -> new ArrayList<>()).addAll(entry.getValue());
        }

        return grouped.entrySet().stream()
            .map(e -> new Benefit(e.getKey(), String.join(", ", e.getValue())))
            .toList();
    }
}
