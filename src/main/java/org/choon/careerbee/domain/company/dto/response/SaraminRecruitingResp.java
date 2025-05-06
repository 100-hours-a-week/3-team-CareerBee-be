package org.choon.careerbee.domain.company.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public record SaraminRecruitingResp(
    Jobs jobs
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Jobs(
        Integer count,
        Integer start,
        String total,
        List<Job> job
    ) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Job(
        String url,
        Integer active,
        Company company,
        Position position,
        String keyword,
        Salary salary,
        Long id,
        String postingTimestamp,
        String postingDate,
        String modificationTimestamp,
        String openingTimestamp,
        String expirationTimestamp,
        String expirationDate,
        CloseType closeType,
        String readCnt,
        String applyCnt
    ) {

    }

    public record Company(Detail detail) {

        public record Detail(String href, String name) {

        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Position(
        String title,
        Industry industry,
        Location location,
        JobType jobType,
        JobMidCode jobMidCode,
        JobCode jobCode,
        ExperienceLevel experienceLevel,
        RequiredEducationLevel requiredEducationLevel,
        String industryKeywordCode,
        String jobCodeKeywordCode
    ) {

        public record Industry(String code, String name) {

        }

        public record Location(String code, String name) {

        }

        public record JobType(String code, String name) {

        }

        public record JobMidCode(String code, String name) {

        }

        public record JobCode(String code, String name) {

        }

        public record ExperienceLevel(Integer code, Integer min, Integer max, String name) {

        }

        public record RequiredEducationLevel(String code, String name) {

        }
    }

    public record CloseType(String code, String name) {

    }

    public record Salary(String code, String name) {

    }
}