package org.choon.careerbee.domain.member.dto.response;

import org.choon.careerbee.domain.member.entity.enums.MajorType;

public record ExtractResumeResp(
    Integer certificationCount,
    Integer projectCount,
    MajorType majorType,
    String companyName,
    Integer workPeriod,
    String position,
    String additionalExperiences
) {

}
