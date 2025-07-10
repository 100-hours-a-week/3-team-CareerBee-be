package org.choon.careerbee.domain.member.dto.response;

import org.choon.careerbee.domain.member.dto.internal.ExtractResumeRespFromAi;
import org.choon.careerbee.domain.member.entity.enums.MajorType;

// [2] camelCase로 응답하는 프론트 전달용 DTO
public record ExtractResumeResp(
    Integer certificationCount,
    Integer projectCount,
    MajorType majorType,
    String companyName,
    Integer workPeriod,
    String position,
    String additionalExperiences
) {

    public static ExtractResumeResp from(ExtractResumeRespFromAi aiResp) {
        return new ExtractResumeResp(
            aiResp.certificationCount(),
            aiResp.projectCount(),
            aiResp.majorType(),
            aiResp.companyName(),
            aiResp.workPeriod(),
            aiResp.position(),
            aiResp.additionalExperiences()
        );
    }
}
