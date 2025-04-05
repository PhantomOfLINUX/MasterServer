package org.codequistify.master.application.account.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.codequistify.master.global.jwt.dto.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenCookieProvider {

    private static final String PROD_DOMAIN = "pol.or.kr";
    private static final String DEV_DOMAIN = "localhost";
    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 60 * 60; // 1 hour
    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7 days

    public void addTokenCookies(HttpServletResponse response, TokenResponse token) {
        addCookie(response, "POL_ACCESS_TOKEN", token.accessToken(), PROD_DOMAIN, ACCESS_TOKEN_EXPIRATION_SECONDS);
        addCookie(response, "POL_REFRESH_TOKEN", token.refreshToken(), PROD_DOMAIN, REFRESH_TOKEN_EXPIRATION_SECONDS);
        addCookie(response, "POL_ACCESS_TOKEN_DEV", token.accessToken(), DEV_DOMAIN, ACCESS_TOKEN_EXPIRATION_SECONDS);
        addCookie(response, "POL_REFRESH_TOKEN_DEV", token.refreshToken(), DEV_DOMAIN, REFRESH_TOKEN_EXPIRATION_SECONDS);
    }

    public void removeTokenCookies(HttpServletResponse response) {
        removeCookie(response, "POL_ACCESS_TOKEN", PROD_DOMAIN);
        removeCookie(response, "POL_REFRESH_TOKEN", PROD_DOMAIN);
        removeCookie(response, "POL_ACCESS_TOKEN_DEV", DEV_DOMAIN);
        removeCookie(response, "POL_REFRESH_TOKEN_DEV", DEV_DOMAIN);
    }

    private void addCookie(HttpServletResponse res, String name, String value, String domain, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        res.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse res, String name, String domain) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }
}