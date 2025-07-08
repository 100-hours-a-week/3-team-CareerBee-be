package org.choon.careerbee.fixture.interview;

import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.member.entity.Member;

public class SolvedInterviewProblemFixture {

    public static SolvedInterviewProblem createSolvedProblem(
        Member member, InterviewProblem interviewProblem, String answer,
        String feedback, SaveStatus saveStatus
    ) {
        return SolvedInterviewProblem.of(
            member, interviewProblem, answer, feedback, saveStatus
        );
    }

}
