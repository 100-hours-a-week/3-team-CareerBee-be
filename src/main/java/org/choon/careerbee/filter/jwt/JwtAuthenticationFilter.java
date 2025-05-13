package org.choon.careerbee.filter.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.entity.enums.TokenStatus;
import org.choon.careerbee.domain.auth.repository.TokenRepository;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String resolveToken = jwtUtil.resolveToken(request.getHeader(AUTHORIZATION));

        if (Objects.equals(resolveToken, "")) {
            request.getRequestDispatcher("/exception/entrypoint/nullToken")
                .forward(request, response);
            return;
        }

        try {
            handleBlacklistedToken(resolveToken);
            Authentication authentication = jwtUtil.getAuthentication(resolveToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            request.getRequestDispatcher("/exception/entrypoint/logout").forward(request, response);
        } catch (ExpiredJwtException e) {
            request.getRequestDispatcher("/exception/entrypoint/expiredToken")
                .forward(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            request.getRequestDispatcher("/exception/entrypoint/badToken")
                .forward(request, response);
        }
    }

    // 로그아웃한 사용자가 접근하는지 파악. -> 접근할경우 예외발생
    private void handleBlacklistedToken(String resolveToken) throws CustomException {
        if (tokenRepository.findByTokenValueAndStatus(resolveToken, TokenStatus.BLACK)
            .isPresent()) {
            throw new CustomException(CustomResponseStatus.LOGOUT_MEMBER);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String[] excludePath = {
            "/auth/oauth",
            "/auth/tokens",
            "/auth/reissue",
            "/favicon.ico",
            "/api/v1/companies"
        };

        String path = request.getRequestURI();
        if (path.equals("/")) {
            return true;
        }

        return Arrays.stream(excludePath).anyMatch(path::contains);
    }
}
