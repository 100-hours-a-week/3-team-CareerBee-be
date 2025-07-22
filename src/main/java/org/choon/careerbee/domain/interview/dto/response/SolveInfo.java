package org.choon.careerbee.domain.interview.dto.response;

import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;

public record SolveInfo(
    Long id,
    String question,
    String answer,
    String feedback,
    SaveStatus saveStatus
) implements MemberInterviewProblemResp {

}
