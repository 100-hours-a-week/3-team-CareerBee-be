package org.choon.careerbee.fixture.competition;

import java.time.LocalDateTime;
import org.choon.careerbee.domain.competition.domain.Competition;

public class CompetitionFixture {

    public static Competition createCompetition(LocalDateTime startDate, LocalDateTime endDate) {
        return Competition.of(startDate, endDate);
    }
}
