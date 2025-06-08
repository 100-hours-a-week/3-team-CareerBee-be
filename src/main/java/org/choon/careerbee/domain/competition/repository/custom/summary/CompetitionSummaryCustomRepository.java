package org.choon.careerbee.domain.competition.repository.custom.summary;

import java.time.LocalDate;
import java.util.List;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;

public interface CompetitionSummaryCustomRepository {

    CompetitionRankingResp fetchRankings(LocalDate today);

    MemberRankingResp fetchMemberRankingById(Long accessMemberId, LocalDate today);

    List<CompetitionSummary> fetchSummaryByPeriodAndType(SummaryPeriod summaryPeriod, SummaryType summaryType);
}
