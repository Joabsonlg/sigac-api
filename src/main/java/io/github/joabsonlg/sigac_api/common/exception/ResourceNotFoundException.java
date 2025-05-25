package io.github.joabsonlg.sigac_api.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Returns HTTP 404 status code.
 */
public class ResourceNotFoundException extends BusinessException {
    
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    
    public ResourceNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(String.format("%s with identifier '%s' not found", resourceType, identifier), ERROR_CODE);
    }
}
