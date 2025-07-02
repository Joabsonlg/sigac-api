package io.github.joabsonlg.sigac_api.auth.dto;

/**
 * DTO for refresh token request.
 * Refresh token is automatically extracted from secure HTTP-only cookie.
 */
public record RefreshTokenRequestDTO() {
    // Empty record - refresh token comes from cookie
}
