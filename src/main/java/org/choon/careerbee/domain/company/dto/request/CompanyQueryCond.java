package org.choon.careerbee.domain.company.dto.request;

import jakarta.annotation.Nullable;

public record CompanyQueryCond(
    @Nullable Integer radius
) {

}
