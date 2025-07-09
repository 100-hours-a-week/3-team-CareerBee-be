package org.choon.careerbee.domain.interview.repository.custom;

import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;

public interface SolvedInterviewProblemCustomRepository {

    SaveInterviewProblemResp fetchSaveProblemIdsByMemberId(Long memberId, Long cursor, int size);
}
