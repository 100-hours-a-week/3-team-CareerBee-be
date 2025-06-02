package org.choon.careerbee.fixture.competition;

import java.time.LocalDate;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.member.entity.Member;

public class CompetitionSummaryFixture {


    public static CompetitionSummary createSummary(
        Member member, Short solvedCount, Long elapsedTime,
        Long ranking,
        SummaryType type, LocalDate periodStart, LocalDate periodEnd
    ) {
        return CompetitionSummary.of(member, solvedCount, elapsedTime, ranking, type, periodStart,
            periodEnd);
    }
}
