package org.choon.careerbee.domain.competition.dto.event;

import java.time.LocalDate;

public record CompetitionJoinedEvent(
    Long competitionId,
    Long memberId
) {

}
