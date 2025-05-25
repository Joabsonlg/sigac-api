package io.github.joabsonlg.sigac_api.common.exception;

/**
 * Exception thrown when a business rule conflict occurs.
 * Returns HTTP 409 status code.
 */
public class ConflictException extends BusinessException {
    
    private static final String ERROR_CODE = "CONFLICT";
    
    public ConflictException(String message) {
        super(message, ERROR_CODE);
    }
    
    public ConflictException(String resourceType, String conflictReason) {
        super(String.format("Conflict with %s: %s", resourceType, conflictReason), ERROR_CODE);
    }
}
