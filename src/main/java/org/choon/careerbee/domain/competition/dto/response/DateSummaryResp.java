package org.choon.careerbee.domain.competition.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DateSummaryResp(
    Long memberId,
    LocalDateTime createdAt
) {

    public LocalDate createdDateOnly() {
        return createdAt.toLocalDate();
    }
}
