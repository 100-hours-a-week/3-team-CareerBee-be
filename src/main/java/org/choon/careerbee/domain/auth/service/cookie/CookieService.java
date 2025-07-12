package org.choon.careerbee.domain.auth.service.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CookieService {

    private static final int REFRESH_MAX_AGE_SEC = 60 * 60 * 24 * 7;
    private static final String COOKIE_NAME = "refreshToken";

    @Value("${server.servlet.session.cookie.domain}")
    private String cookieDomain;

    /* ───────────────────────────── 쿠키 세팅 ───────────────────────────── */

    public void setRefreshTokenCookie(HttpServletResponse resp, AuthTokens tokens, String origin) {
        resp.addCookie(buildCookie(tokens.refreshToken(), REFRESH_MAX_AGE_SEC, origin));
    }

    public void deleteRefreshTokenCookie(HttpServletResponse resp, String origin) {
        resp.addCookie(buildCookie("", 0, origin));
    }

    /* ───────────────────────────── 헬퍼 ───────────────────────────── */

    private Cookie buildCookie(String value, int maxAge, String origin) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        log.info("[Cookie 로직] origin : {}", origin);

        if (!"http://localhost:5173".equals(origin) && !"https://localhost:5173".equals(origin)) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

}
