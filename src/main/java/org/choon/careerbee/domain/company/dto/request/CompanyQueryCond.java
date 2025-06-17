package org.choon.careerbee.domain.company.dto.request;

import jakarta.annotation.Nullable;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;

public record CompanyQueryCond(
    @Nullable Integer radius,
    @Nullable RecruitingStatus recruitingStatus,
    @Nullable BusinessType type
) {

}
