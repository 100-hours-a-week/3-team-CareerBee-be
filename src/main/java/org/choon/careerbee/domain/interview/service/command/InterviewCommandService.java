package org.choon.careerbee.domain.interview.service.command;

import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.request.SubmitAnswerReq;

public interface InterviewCommandService {

    void saveInterviewProblem(Long problemIdToSave, Long accessMemberId);

    void cancelSaveInterviewProblem(Long problemIdToCancelSave, Long accessMemberId);

    void submitAnswerAsync(SubmitAnswerReq submitAnswerReq, Long accessMemberId);

    void requestNextProblem(ProblemType type, Long accessMemberId);
}
