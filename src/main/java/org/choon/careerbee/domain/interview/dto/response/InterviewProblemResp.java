package org.choon.careerbee.domain.interview.dto.response;

import java.util.List;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;

public record InterviewProblemResp(
    List<InterviewProblemInfo> interviewProblems
) {

    public record InterviewProblemInfo(
        ProblemType type,
        String question
    ) {

    }
}
