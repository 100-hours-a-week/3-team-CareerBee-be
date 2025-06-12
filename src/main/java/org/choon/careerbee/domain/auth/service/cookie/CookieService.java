package org.choon.careerbee.domain.auth.service.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    private static final int REFRESH_MAX_AGE_SEC = 60 * 60 * 24 * 7;
    private static final String COOKIE_NAME = "refreshToken";

    @Value("${server.servlet.session.cookie.domain}")
    private String cookieDomain;

    /* ───────────────────────────── 쿠키 세팅 ───────────────────────────── */

    public void setRefreshTokenCookie(HttpServletResponse resp, AuthTokens tokens) {
        resp.addCookie(buildCookie(tokens.refreshToken(), REFRESH_MAX_AGE_SEC));
    }

    public void deleteRefreshTokenCookie(HttpServletResponse resp) {
        resp.addCookie(buildCookie("", 0));
    }

    /* ───────────────────────────── 헬퍼 ───────────────────────────── */

    private Cookie buildCookie(String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setDomain(cookieDomain);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

}
