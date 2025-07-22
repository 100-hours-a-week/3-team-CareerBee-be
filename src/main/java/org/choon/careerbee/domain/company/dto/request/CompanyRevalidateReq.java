package org.choon.careerbee.domain.company.dto.request;

import java.util.List;

public record CompanyRevalidateReq(
    List<Long> companyIds
) {

}
