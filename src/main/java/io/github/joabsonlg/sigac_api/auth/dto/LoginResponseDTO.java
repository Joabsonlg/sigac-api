package io.github.joabsonlg.sigac_api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for login response.
 * Contains authentication token and user information.
 * Refresh token is sent via secure HTTP-only cookie.
 */
public record LoginResponseDTO(
        @JsonProperty("token")
        String token,
        
        @JsonProperty("tokenType")
        String tokenType,
        
        @JsonProperty("expiresIn")
        Long expiresIn,
        
        @JsonProperty("user")
        UserInfoDTO user
) {
    /**
     * Constructor for creating login response.
     *
     * @param token     JWT access token
     * @param tokenType token type (usually "Bearer")
     * @param expiresIn token expiration time in seconds
     * @param user      user information
     */
    public LoginResponseDTO {
        // Default token type if not provided
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
}
