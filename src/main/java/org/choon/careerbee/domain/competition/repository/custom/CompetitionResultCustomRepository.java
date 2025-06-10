package org.choon.careerbee.domain.competition.repository.custom;

import java.time.LocalDate;
import java.util.Optional;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;

public interface CompetitionResultCustomRepository {

    Optional<MemberLiveRankingResp> fetchMemberLiveRankingByDate(
        Long accessMemberId, LocalDate today
    );

    LiveRankingResp fetchLiveRankingByDate(LocalDate today);
}
