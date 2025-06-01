package org.choon.careerbee.domain.member.dto.response;

import java.util.List;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;

public record WishCompaniesResp(
    List<CompanySummaryInfo> wishCompanies,
    Long nextCursor,
    boolean hasNext
) {

}
