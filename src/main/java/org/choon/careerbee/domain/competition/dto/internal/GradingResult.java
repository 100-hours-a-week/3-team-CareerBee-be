package org.choon.careerbee.domain.competition.dto.internal;

import java.util.List;
import org.choon.careerbee.domain.competition.dto.response.CompetitionGradingResp.CompetitionGradingInfo;

public record GradingResult(
    List<CompetitionGradingInfo> gradingInfos,
    short correctCount
) {

}
