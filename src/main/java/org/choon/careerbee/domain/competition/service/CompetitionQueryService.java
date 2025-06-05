package org.choon.careerbee.domain.competition.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;

public interface CompetitionQueryService {

    CompetitionParticipationResp checkCompetitionParticipationById(
        Long competitionId, Long accessMemberId
    );

    CompetitionProblemResp fetchProblems(Long competitionId);

    CompetitionRankingResp fetchRankings(LocalDateTime today);

    CompetitionIdResp fetchCompetitionIdBy(LocalDate today);

    MemberRankingResp fetchMemberCompetitionRankingById(Long accessMemberId);
}
