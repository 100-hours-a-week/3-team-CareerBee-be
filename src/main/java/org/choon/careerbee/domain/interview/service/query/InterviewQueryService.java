package org.choon.careerbee.domain.interview.service.query;

import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemDetailResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;

public interface InterviewQueryService {

    InterviewProblemResp fetchInterviewProblem();

    CheckProblemSolveResp checkInterviewProblemSolved(Long problemId, Long accessMemberId);

    InterviewProblem findById(Long problemId);

    SolvedInterviewProblem findSolvedProblemById(Long problemId, Long memberId);

    SaveInterviewProblemResp fetchSaveInterviewProblem(Long accessMemberId, Long cursor, int size);

    InterviewProblemDetailResp fetchMemberInterviewProblemByType(
        ProblemType problemType, Long accessMemberId
    );
}
