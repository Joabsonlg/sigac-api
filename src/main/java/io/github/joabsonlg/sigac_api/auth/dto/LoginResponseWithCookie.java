package io.github.joabsonlg.sigac_api.auth.dto;

import org.springframework.http.ResponseCookie;

/**
 * Container for login response with cookie.
 * Used internally to return both response data and refresh token cookie.
 */
public record LoginResponseWithCookie(
        LoginResponseDTO response,
        ResponseCookie refreshTokenCookie
) {}
