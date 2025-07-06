package org.choon.careerbee.domain.company.dto.internal;

import java.util.List;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;

public record CompanyRecruitInfo(
    RecruitingStatus recruitingStatus,
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
