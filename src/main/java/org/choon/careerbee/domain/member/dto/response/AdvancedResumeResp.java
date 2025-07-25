package org.choon.careerbee.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",       // JSON에 "type": "in-progress" 또는 "complete" 같이 붙음
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ResumeInProgressResp.class, name = "in-progress"),
    @JsonSubTypes.Type(value = ResumeCompleteResp.class, name = "complete")
})
public sealed interface AdvancedResumeResp
    permits ResumeInProgressResp, ResumeCompleteResp {

    String message();
}
