package org.choon.careerbee.domain.competition.repository.custom;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;

public interface CompetitionSummaryCustomRepository {

    CompetitionRankingResp fetchRankings(LocalDate today);

    MemberRankingResp fetchMemberRankingById(Long accessMemberId, LocalDate today);
}
