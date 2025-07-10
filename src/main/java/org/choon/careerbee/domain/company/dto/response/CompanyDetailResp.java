package org.choon.careerbee.domain.company.dto.response;

import java.util.List;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart.Benefit;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart.Financials;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart.Photo;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart.TechStack;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;

public record CompanyDetailResp(
    Long id,
    String name,
    String title,
    String logoUrl,
    CompanyType companyType,
    RecruitingStatus recruitingStatus,
    String address,
    Integer employeeCount,
    String homepageUrl,
    String description,
    Double rating,
    Financials financials,
    List<Photo> photos,
    List<Benefit> benefits,
    List<TechStack> techStacks
//    List<Recruitment> recruitments
) {

    public static CompanyDetailResp of(
        CompanyStaticPart staticPart,
        RecruitingStatus recruitingStatus
    ) {
        return new CompanyDetailResp(
            staticPart.id(),
            staticPart.name(),
            staticPart.title(),
            staticPart.logoUrl(),
            staticPart.companyType(),
            recruitingStatus,
            staticPart.address(),
            staticPart.employeeCount(),
            staticPart.homepageUrl(),
            staticPart.description(),
            staticPart.rating(),
            staticPart.financials(),
            staticPart.photos(),
            staticPart.benefits(),
            staticPart.techStacks()
        );
    }
}
