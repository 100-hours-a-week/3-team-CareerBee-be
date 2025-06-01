package org.choon.careerbee.fixture;

import org.choon.careerbee.domain.competition.domain.problem.CompetitionProblem;
import org.choon.careerbee.domain.competition.domain.problem.ProblemChoice;

public class ProblemChoiceFixture {

    public static ProblemChoice createProblemChoice(CompetitionProblem competitionProblem, String content, Short choiceOrder) {
        return ProblemChoice.of(competitionProblem, content, choiceOrder);
    }
}
