package io.github.joabsonlg.sigac_api.auth.exception;

import io.github.joabsonlg.sigac_api.common.exception.BusinessException;

/**
 * Exception thrown when authentication fails.
 * Returns HTTP 401 status code.
 */
public class AuthenticationException extends BusinessException {
    
    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
