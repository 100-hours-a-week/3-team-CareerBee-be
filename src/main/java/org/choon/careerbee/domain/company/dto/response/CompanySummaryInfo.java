package org.choon.careerbee.domain.company.dto.response;

import java.util.List;
import org.choon.careerbee.domain.company.dto.internal.CompanySummaryInfoWithoutWish;

public record CompanySummaryInfo(
    Long id,
    String name,
    String logoUrl,
    Long wishCount,
    List<Keyword> keywords
) {

    public record Keyword(
        String content
    ) {

    }

    public static CompanySummaryInfo of(
        CompanySummaryInfoWithoutWish companySimpleInfo,
        Long wishCount
    ) {
        return new CompanySummaryInfo(
            companySimpleInfo.id(),
            companySimpleInfo.name(),
            companySimpleInfo.logoUrl(),
            wishCount,
            companySimpleInfo.keywords()
        );
    }
}
