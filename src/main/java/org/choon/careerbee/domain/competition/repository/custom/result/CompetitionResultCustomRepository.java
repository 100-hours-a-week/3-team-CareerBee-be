package org.choon.careerbee.domain.competition.repository.custom.result;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.DateSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.ResultSummaryResp;

public interface CompetitionResultCustomRepository {

    List<DailyResultSummaryResp> fetchResultSummaryOfDaily(LocalDate summaryDate);

    List<ResultSummaryResp> fetchResultSummaryByPeriod(SummaryPeriod summaryPeriod);

    List<DateSummaryResp> fetchDateSummaryIn(SummaryPeriod summaryPeriod,
        List<Long> summaryMemberIds);

    Optional<MemberLiveRankingResp> fetchMemberLiveRankingByDate(
        Long accessMemberId, LocalDate today
    );

    LiveRankingResp fetchLiveRankingByDate(LocalDate today);
}
