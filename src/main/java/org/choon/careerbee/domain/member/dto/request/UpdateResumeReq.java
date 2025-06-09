package org.choon.careerbee.domain.member.dto.request;

import org.choon.careerbee.domain.member.entity.enums.MajorType;

public record UpdateResumeReq(
    String psTier,
    int certificationCount,
    int projectCount,
    MajorType majorType,
    String companyName,
    int workPeriod,
    String position,
    String additionalExperiences
) {

}
