package org.choon.careerbee.domain.auth.dto.jwt;

import lombok.Builder;

@Builder
public record TokenInfo(
    Long id,
    String role
) {

}
