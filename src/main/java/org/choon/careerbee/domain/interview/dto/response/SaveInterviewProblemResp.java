package org.choon.careerbee.domain.interview.dto.response;

import java.util.List;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;

public record SaveInterviewProblemResp(

    List<SaveProblemInfo> savedProblems,
    Long nextCursor,
    boolean hasNext
) {

    public record SaveProblemInfo(
        Long id,
        ProblemType type,
        String question,
        String answer,
        String feedback
    ) {

    }
}
