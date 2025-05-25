package io.github.joabsonlg.sigac_api.common.exception;

/**
 * Exception thrown when validation of input data fails.
 * Returns HTTP 400 status code.
 */
public class ValidationException extends BusinessException {
    
    private static final String ERROR_CODE = "VALIDATION_ERROR";
    
    public ValidationException(String message) {
        super(message, ERROR_CODE);
    }
    
    public ValidationException(String field, String reason) {
        super(String.format("Validation failed for field '%s': %s", field, reason), ERROR_CODE);
    }
}
