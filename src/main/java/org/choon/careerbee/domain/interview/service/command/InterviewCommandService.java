package org.choon.careerbee.domain.interview.service.command;

public interface InterviewCommandService {

    void saveInterviewProblem(Long problemIdToSave, Long accessMemberId);

    void cancelSaveInterviewProblem(Long problemIdToCancelSave, Long accessMemberId);
}
