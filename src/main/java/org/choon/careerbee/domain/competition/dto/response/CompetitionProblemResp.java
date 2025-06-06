package org.choon.careerbee.domain.competition.dto.response;

import java.util.List;

public record CompetitionProblemResp(
    List<ProblemInfo> problems
) {

    public record ProblemInfo(
        int number,
        String title,
        String description,
        String solution,
        int answer,
        List<ProblemChoiceInfo> choices
    ) {

    }

    public record ProblemChoiceInfo(
        int order,
        String content
    ) {

    }
}
