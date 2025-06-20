package org.choon.careerbee.domain.competition.dto.request;

import java.util.List;

public record CompetitionResultSubmitReq(
    Integer elapsedTime,
    List<SubmitInfo> submittedAnswers
) {

    public record SubmitInfo(
        Long problemId,
        Short userChoice
    ) {

    }

}
