package api.utils;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.ResponseCookie;

import api.entities.RefreshToken;

/**
 * {@link CookieUtils}.
 */
public class CookieUtils {
    public static final String REFRESH_COOKIE_NAME = "refresh_token";

    /**
     * Generate empty response cookie.
     *
     * @return {@link ResponseCookie}
     */
    public static ResponseCookie generateEmptyCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();
    }

    /**
     * Generate response cookie with refresh token.
     *
     * @param refreshToken Refresh token
     * @return {@link ResponseCookie}
     */
    public static ResponseCookie generateRefreshTokenCookie(RefreshToken refreshToken) {
        long maxAge = Duration.between(Instant.now(), refreshToken.getExpires()).getSeconds();
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken.getToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(maxAge)
            .sameSite("Strict")
            .build();
    }
}
