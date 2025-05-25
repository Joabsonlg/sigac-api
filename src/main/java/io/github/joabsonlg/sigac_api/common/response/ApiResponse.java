package io.github.joabsonlg.sigac_api.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Standard success response wrapper for the API.
 * Provides consistent success response structure with metadata.
 *
 * @param <T> the type of data being returned
 */
public record ApiResponse<T>(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp,
        String message,
        T data
) {
    
    /**
     * Creates a success response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(LocalDateTime.now(), "Success", data);
    }
    
    /**
     * Creates a success response with custom message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), message, data);
    }
    
    /**
     * Creates a success response with only a message (no data).
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(LocalDateTime.now(), message, null);
    }
}
