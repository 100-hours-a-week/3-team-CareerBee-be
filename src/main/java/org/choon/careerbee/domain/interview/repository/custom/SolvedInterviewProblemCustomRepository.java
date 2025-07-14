package org.choon.careerbee.domain.interview.repository.custom;

import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.response.ProblemInfo;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.SolveInfo;

public interface SolvedInterviewProblemCustomRepository {

    SaveInterviewProblemResp fetchSaveProblemIdsByMemberId(Long memberId, Long cursor, int size);

    SolveInfo fetchSolveProblemInfoByTypeAndMemberId(ProblemType problemType, Long memberId);

    ProblemInfo fetchNextProblemByTypeAndMemberId(ProblemType problemType, Long accessMemberId);
}
