package org.choon.careerbee.domain.company.dto.internal;

import java.util.List;

public record CompanyRecruitInfo(
    List<Recruitment> recruitments
) {

    public record Recruitment(
        Long id,
        String url,
        String title,
        String startDate,
        String endDate
    ) {

    }
}
