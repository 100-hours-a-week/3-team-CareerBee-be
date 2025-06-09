package org.choon.careerbee.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.entity.enums.PreferredJob;

@Builder
public record ResumeDraftReq(
    @JsonProperty("email") String email,
    @JsonProperty("preferred_job") PreferredJob preferredJob,
    @JsonProperty("certification_count") int certificationCount,
    @JsonProperty("project_count") int projectCount,
    @JsonProperty("major_type") MajorType majorType,
    @JsonProperty("company_name") String companyName,
    @JsonProperty("work_period") int workPeriod,
    @JsonProperty("position") String position,
    @JsonProperty("additional_experiences") String additionalExperiences
) {

    public static ResumeDraftReq from(Member member) {
        return ResumeDraftReq.builder()
            .email(member.getEmail())
            .preferredJob(member.getPreferredJob())
            .certificationCount(member.getCertificationCount())
            .projectCount(member.getProjectCount())
            .majorType(member.getMajorType())
            .companyName(member.getCompanyName())
            .workPeriod(member.getWorkPeriod())
            .position(member.getPosition())
            .additionalExperiences(member.getAdditionalExperiences())
            .build();
    }
}