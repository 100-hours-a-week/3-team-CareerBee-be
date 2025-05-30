package org.choon.careerbee.domain.competition.service;

import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;

public interface CompetitionQueryService {

    CompetitionParticipationResp checkCompetitionParticipationById(
        Long competitionId, Long accessMemberId
    );
}
