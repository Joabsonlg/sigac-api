package io.github.joabsonlg.sigac_api.auth.dto;

import org.springframework.http.ResponseCookie;

/**
 * Container for login response with cookies.
 * Used internally to return both response data and authentication cookies.
 */
public record LoginResponseWithCookie(
        CookieLoginResponseDTO response,
        ResponseCookie accessTokenCookie,
        ResponseCookie refreshTokenCookie
) {}
