package org.choon.careerbee.domain.company.dto.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.choon.careerbee.domain.company.entity.Company;

public record JobContext(
    Map<String, Company> companyMap,
    Set<Long> existingIds,
    Map<Long, List<Long>> wishMemberMap
) {

}
