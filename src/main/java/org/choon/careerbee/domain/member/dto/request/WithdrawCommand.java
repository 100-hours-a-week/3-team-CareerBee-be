package org.choon.careerbee.domain.member.dto.request;

import java.time.LocalDateTime;

public record WithdrawCommand(
    String reason,
    LocalDateTime requestedAt
) {

}
