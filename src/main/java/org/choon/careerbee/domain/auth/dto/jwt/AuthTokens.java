package org.choon.careerbee.domain.auth.dto.jwt;

import lombok.Builder;

@Builder
public record AuthTokens(
    String accessToken,
    String refreshToken
) {

    public static AuthTokens of(String accessToken, String refreshToken) {
        return AuthTokens.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}