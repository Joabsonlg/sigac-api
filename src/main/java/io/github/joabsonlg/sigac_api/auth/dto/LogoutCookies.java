package io.github.joabsonlg.sigac_api.auth.dto;

import org.springframework.http.ResponseCookie;

/**
 * Container for logout cookies.
 * Used to return multiple cookies that need to be cleared on logout.
 */
public record LogoutCookies(
        ResponseCookie accessTokenCookie,
        ResponseCookie refreshTokenCookie
) {}
