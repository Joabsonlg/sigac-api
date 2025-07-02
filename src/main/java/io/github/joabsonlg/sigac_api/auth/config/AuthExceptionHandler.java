package io.github.joabsonlg.sigac_api.auth.config;

import io.github.joabsonlg.sigac_api.auth.exception.AuthenticationException;
import io.github.joabsonlg.sigac_api.common.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Global exception handler for authentication-related exceptions.
 * Extends the common exception handling with auth-specific errors.
 */
@RestControllerAdvice
public class AuthExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthExceptionHandler.class);
    
    /**
     * Handles AuthenticationException and returns 401 status.
     */
    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAuthentication(AuthenticationException ex) {
        logger.warn("Authentication error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }
}
