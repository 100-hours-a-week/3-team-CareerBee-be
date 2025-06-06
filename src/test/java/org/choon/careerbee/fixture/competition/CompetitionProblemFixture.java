package org.choon.careerbee.fixture.competition;

import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.problem.CompetitionProblem;

public class CompetitionProblemFixture {

    public static CompetitionProblem createProblem(
        Competition competition, String title, String description,
        String solution, Short answer
    ) {
        return CompetitionProblem.of(
            competition, title, description, solution, answer
        );
    }

}
