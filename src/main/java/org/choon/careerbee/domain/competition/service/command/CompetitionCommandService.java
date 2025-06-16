package org.choon.careerbee.domain.competition.service.command;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;

public interface CompetitionCommandService {

    void joinCompetition(Long competitionId, Long accessMemberId);

    void submitCompetitionResult(Long competitionId, CompetitionResultSubmitReq submitReq,
        Long accessMemberId);

    void rewardToWeekRanker(LocalDate now);
}
