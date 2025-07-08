package org.choon.careerbee.filter.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BL_KEY = "bl:";

    private final JwtUtil jwtUtil;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String resolveToken = jwtUtil.resolveToken(request.getHeader(AUTHORIZATION));

        if (Objects.equals(resolveToken, "")) {
            writeErrorResponse(response, CustomResponseStatus.NULL_JWT);
            return;
        }

        try {
            handleBlacklistedToken(resolveToken);
            Authentication authentication = jwtUtil.getAuthentication(resolveToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            writeErrorResponse(response, e.getCustomResponseStatus());
        } catch (ExpiredJwtException e) {
            writeErrorResponse(response, CustomResponseStatus.EXPIRED_JWT);
        } catch (JwtException | IllegalArgumentException e) {
            writeErrorResponse(response, CustomResponseStatus.BAD_JWT);
        }
    }

    private void handleBlacklistedToken(String resolveToken) throws CustomException {
        boolean isBlack = redissonClient.getBucket(BL_KEY + resolveToken).isExists();

        if (isBlack) {
            throw new CustomException(CustomResponseStatus.LOGOUT_MEMBER);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, CustomResponseStatus status)
        throws IOException {
        response.setStatus(status.getHttpStatusCode());
        response.setContentType("application/json;charset=UTF-8");

        String responseBody = objectMapper.writeValueAsString(CommonResponse.createError(status));
        response.getWriter().write(responseBody);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String[] excludePath = {
            "/health-check",
            "/auth/oauth",
            "/auth/tokens",
            "/auth/reissue",
            "/competitions/rankings",
            "/competitions/ids",
            "/favicon.ico",
            "/api/v1/companies",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/tickets"
        };

        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path.equals("/api/v1/members/competitions/rankings") || path.equals(
            "/api/v1/members/competitions/rankings/live")) {
            return false;
        }

        if (path.equals("/api/v1/tickets") && method.equals("POST")
            ||
            path.equals("/api/v1/members/tickets")
        ) {
            return false;
        }
        return Arrays.stream(excludePath).anyMatch(path::contains);
    }
}
