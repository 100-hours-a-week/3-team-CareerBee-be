package org.choon.careerbee.domain.competition.dto.internal;

public record ProblemAnswerInfo(
    Long problemId,
    short answer,
    String solution
) {

}
