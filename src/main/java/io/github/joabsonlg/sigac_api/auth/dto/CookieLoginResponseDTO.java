package io.github.joabsonlg.sigac_api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for cookie-based login response.
 * Contains only user information since tokens are sent via HTTP-only cookies.
 */
public record CookieLoginResponseDTO(
        @JsonProperty("message")
        String message,
        
        @JsonProperty("user")
        UserInfoDTO user
) {
    /**
     * Constructor for creating cookie-based login response.
     *
     * @param message success message
     * @param user    user information
     */
    public CookieLoginResponseDTO {
        // Record validation can be added here if needed
    }
    
    /**
     * Creates a successful login response.
     *
     * @param user user information
     * @return login response
     */
    public static CookieLoginResponseDTO success(UserInfoDTO user) {
        return new CookieLoginResponseDTO("Login successful", user);
    }
}
