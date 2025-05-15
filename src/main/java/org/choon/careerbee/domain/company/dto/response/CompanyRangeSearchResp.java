package org.choon.careerbee.domain.company.dto.response;

import java.util.List;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;

public record CompanyRangeSearchResp(
    List<CompanyMarkerInfo> companies
) {

    public record CompanyMarkerInfo(
        Long id,
        String markerUrl,
        BusinessType businessType,
        RecruitingStatus recruitingStatus,
        LocationInfo locationInfo
    ) {

    }

    public record LocationInfo(
        double latitude,
        double longitude
    ) {

    }
}