package org.choon.careerbee.fixture.interview;

import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;

public class InterviewProblemFixture {

    public static InterviewProblem createInterviewProblem(
        String question, ProblemType type
    ) {
        return InterviewProblem.of(question, type);
    }

}
