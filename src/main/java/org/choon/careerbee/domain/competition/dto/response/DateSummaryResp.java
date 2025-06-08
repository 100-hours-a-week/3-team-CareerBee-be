package org.choon.careerbee.domain.competition.dto.response;

import java.time.LocalDate;

public record DateSummaryResp(
    Long memberId,
    LocalDate participationDate
) {

}
