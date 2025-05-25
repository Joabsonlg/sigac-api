package io.github.joabsonlg.sigac_api.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Standard error response structure for the API.
 * Provides consistent error information across all endpoints.
 */
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String errorCode
) {
    
    /**
     * Builder pattern for creating ErrorResponse instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String errorCode;
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder status(int status) {
            this.status = status;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(timestamp, status, error, message, errorCode);
        }
    }
}
