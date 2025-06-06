package org.choon.careerbee.fixture.competition;

import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.member.entity.Member;

public class CompetitionResultFixture {

    public static CompetitionResult createCompetitionResult(Competition competition, Member member,
        CompetitionResultSubmitReq req) {
        return CompetitionResult.of(competition, member, req);
    }

}
