package org.choon.careerbee.domain.company.dto.internal;

import java.util.List;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo.Keyword;

public record CompanySummaryInfoWithoutWish(
    Long id,
    String name,
    String logoUrl,
    List<Keyword> keywords
) {

}
