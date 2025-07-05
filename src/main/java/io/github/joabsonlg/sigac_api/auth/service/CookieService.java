package io.github.joabsonlg.sigac_api.auth.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service for managing cookies in authentication.
 * Handles secure HTTP-only cookies for refresh tokens.
 */
@Service
public class CookieService {
    
    private static final String REFRESH_TOKEN_COOKIE_NAME = "sigac_refresh_token";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "sigac_access_token";
    private static final String COOKIE_PATH = "/";
    private static final boolean SECURE = false; // Use false in development with HTTP
    private static final boolean HTTP_ONLY = true;
    private static final String SAME_SITE = "Lax"; // Less restrictive for CORS
    
    /**
     * Creates a secure HTTP-only cookie for refresh token.
     *
     * @param refreshToken the refresh token value
     * @param maxAge       cookie max age in seconds
     * @return ResponseCookie for refresh token
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite(SAME_SITE)
                .build();
    }
    
    /**
     * Creates a secure HTTP-only cookie for access token.
     *
     * @param accessToken the access token value
     * @param maxAge      cookie max age in seconds
     * @return ResponseCookie for access token
     */
    public ResponseCookie createAccessTokenCookie(String accessToken, Duration maxAge) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite(SAME_SITE)
                .build();
    }
    
    /**
     * Creates a cookie to clear the refresh token (for logout).
     *
     * @return ResponseCookie that clears the refresh token
     */
    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .sameSite(SAME_SITE)
                .build();
    }
    
    /**
     * Creates a cookie to clear the access token (for logout).
     *
     * @return ResponseCookie that clears the access token
     */
    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .sameSite(SAME_SITE)
                .build();
    }
    
    /**
     * Gets the refresh token cookie name.
     *
     * @return cookie name
     */
    public String getRefreshTokenCookieName() {
        return REFRESH_TOKEN_COOKIE_NAME;
    }
    
    /**
     * Gets the access token cookie name.
     *
     * @return cookie name
     */
    public String getAccessTokenCookieName() {
        return ACCESS_TOKEN_COOKIE_NAME;
    }
}
