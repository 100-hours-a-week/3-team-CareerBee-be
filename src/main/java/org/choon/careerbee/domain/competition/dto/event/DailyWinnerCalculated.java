package org.choon.careerbee.domain.competition.dto.event;

import java.time.LocalDate;
import java.util.List;

public record DailyWinnerCalculated(
    String winnerNickname,
    LocalDate day,
    List<Long> targetMemberIds
) {

}
