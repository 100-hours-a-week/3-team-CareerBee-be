package org.choon.careerbee.domain.interview.service.query;

import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;

public interface InterviewQueryService {

    InterviewProblemResp fetchInterviewProblem();

    CheckProblemSolveResp checkInterviewProblemSolved(Long problemId, Long accessMemberId);
}
