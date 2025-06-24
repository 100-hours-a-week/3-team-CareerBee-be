package org.choon.careerbee.domain.competition.service.command;

import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.dto.response.CompetitionGradingResp;

public interface CompetitionCommandService {

    void joinCompetition(Long competitionId, Long accessMemberId);

    CompetitionGradingResp submitCompetitionResult(
        Long competitionId,
        CompetitionResultSubmitReq submitReq,
        Long accessMemberId
    );
}
