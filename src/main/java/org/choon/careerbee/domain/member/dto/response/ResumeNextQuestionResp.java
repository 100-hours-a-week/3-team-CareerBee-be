package org.choon.careerbee.domain.member.dto.response;

public record ResumeNextQuestionResp(
    Long memberId,
    boolean isComplete,
    String question
) implements AdvancedResumeResp {

}
