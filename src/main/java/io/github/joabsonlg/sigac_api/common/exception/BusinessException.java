package io.github.joabsonlg.sigac_api.common.exception;

/**
 * Base exception class for all business logic exceptions in the application.
 * Provides a common structure for handling application-specific errors.
 */
public abstract class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
