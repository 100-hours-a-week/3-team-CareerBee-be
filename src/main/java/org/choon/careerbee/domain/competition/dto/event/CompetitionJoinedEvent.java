package org.choon.careerbee.domain.competition.dto.event;

public record CompetitionJoinedEvent(
    Long competitionId,
    Long memberId
) {

}
