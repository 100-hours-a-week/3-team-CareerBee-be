package org.choon.careerbee.domain.auth.dto.jwt;

import lombok.Builder;

@Builder
public record TokenClaimInfo(
    Long id
) {

}
