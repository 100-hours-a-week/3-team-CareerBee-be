
package org.choon.careerbee.util.jwt;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private final JwtUtil jwtUtil;

    public AuthTokens generateToken(Long id) {
        String accessToken = jwtUtil.createToken(id, TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createToken(id, TokenType.REFRESH_TOKEN);

        return AuthTokens.of(accessToken, refreshToken);
    }

    public AuthTokens generateTokenWithRF(Long id, String refreshToken) {
        String accessToken = jwtUtil.createToken(id, TokenType.ACCESS_TOKEN);

        return AuthTokens.of(accessToken, refreshToken);
    }
}
