package org.choon.careerbee.domain.competition.service;

import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;

public interface CompetitionCommandService {

    void joinCompetition(Long competitionId, Long accessMemberId);

    void submitCompetitionResult(Long competitionId, CompetitionResultSubmitReq submitReq,
        Long accessMemberId);
}
