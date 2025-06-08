package org.choon.careerbee.domain.competition.dto.request;

import java.time.LocalDate;

public record SummaryPeriod(
    LocalDate startAt,
    LocalDate endAt
) {

}
