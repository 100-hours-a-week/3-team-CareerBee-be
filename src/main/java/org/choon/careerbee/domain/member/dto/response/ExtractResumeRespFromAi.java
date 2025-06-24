package org.choon.careerbee.domain.member.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import org.choon.careerbee.domain.member.entity.enums.MajorType;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ExtractResumeRespFromAi(
    Integer certificationCount,
    Integer projectCount,
    MajorType majorType,
    String companyName,
    Integer workPeriod,
    String position,
    String additionalExperiences
) {

}
