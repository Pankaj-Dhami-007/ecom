package com.dhami.authservice.security;

import com.dhami.authservice.config.JwtProperties;
import com.dhami.authservice.constants.Routes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CookieService {

    private static final String ACCESS_COOKIE  = "access_token";
    private static final String REFRESH_COOKIE = "refresh_token";

    private final JwtProperties jwtProperties;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        response.addCookie(buildCookie(
                ACCESS_COOKIE,
                token,
                Routes.ROOT_PATH,
                (int) jwtProperties.accessTtlSeconds()
        ));
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        response.addCookie(buildCookie(
                REFRESH_COOKIE,
                token,
                Routes.Auth.REFRESH,
                (int) jwtProperties.refreshTtlSeconds()
        ));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(buildCookie(ACCESS_COOKIE,  "", Routes.ROOT_PATH,      0));
        response.addCookie(buildCookie(REFRESH_COOKIE, "", Routes.Auth.REFRESH, 0));
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, ACCESS_COOKIE);
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, REFRESH_COOKIE);
    }

    private Cookie buildCookie(String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    private Optional<String> extractCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
