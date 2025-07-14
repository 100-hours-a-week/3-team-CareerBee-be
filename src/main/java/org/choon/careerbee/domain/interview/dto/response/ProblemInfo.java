package org.choon.careerbee.domain.interview.dto.response;

public record ProblemInfo(
    Long id,
    String question
) implements MemberInterviewProblemResp {

}
