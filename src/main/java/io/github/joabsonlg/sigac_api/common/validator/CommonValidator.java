package io.github.joabsonlg.sigac_api.common.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Common validation utilities for the application.
 * Provides reusable validation methods for common data types.
 */
@Component
public class CommonValidator {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$|^\\(?\\d{2,3}\\)?[\\s.-]?\\d{4,5}[\\s.-]?\\d{4}$"
    );
    
    private static final Pattern CPF_PATTERN = Pattern.compile(
            "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$"
    );
    
    private static final Pattern CNPJ_PATTERN = Pattern.compile(
            "^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$"
    );
    
    /**
     * Validates that a string is not null or empty.
     */
    public void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "is required");
        }
    }
    
    /**
     * Validates email format.
     */
    public void validateEmail(String email, String fieldName) {
        validateRequired(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(fieldName, "must be a valid email address");
        }
    }
    
    /**
     * Validates phone number format.
     */
    public void validatePhone(String phone, String fieldName) {
        validateRequired(phone, fieldName);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(fieldName, "must be a valid phone number");
        }
    }
    
    /**
     * Validates CPF format and digits.
     */
    public void validateCpf(String cpf, String fieldName) {
        validateRequired(cpf, fieldName);
        
        String cleanCpf = cpf.replaceAll("[^\\d]", "");
        
        if (!CPF_PATTERN.matcher(cpf).matches() || !isValidCpf(cleanCpf)) {
            throw new ValidationException(fieldName, "must be a valid CPF");
        }
    }
    
    /**
     * Validates CNPJ format and digits.
     */
    public void validateCnpj(String cnpj, String fieldName) {
        validateRequired(cnpj, fieldName);
        
        String cleanCnpj = cnpj.replaceAll("[^\\d]", "");
        
        if (!CNPJ_PATTERN.matcher(cnpj).matches() || !isValidCnpj(cleanCnpj)) {
            throw new ValidationException(fieldName, "must be a valid CNPJ");
        }
    }
    
    /**
     * Validates string length range.
     */
    public void validateLength(String value, String fieldName, int minLength, int maxLength) {
        validateRequired(value, fieldName);
        
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException(fieldName, 
                    String.format("must be between %d and %d characters", minLength, maxLength));
        }
    }
    
    /**
     * Validates that a number is positive.
     */
    public void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName, "must be positive");
        }
    }
    
    /**
     * Validates that a number is not negative.
     */
    public void validateNonNegative(Number value, String fieldName) {
        if (value == null || value.doubleValue() < 0) {
            throw new ValidationException(fieldName, "must not be negative");
        }
    }
    
    /**
     * Helper method to validate CPF digits.
     */
    private boolean isValidCpf(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        try {
            // Calculate first check digit
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;
            
            // Calculate second check digit
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;
            
            return Character.getNumericValue(cpf.charAt(9)) == firstDigit &&
                   Character.getNumericValue(cpf.charAt(10)) == secondDigit;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Helper method to validate CNPJ digits.
     */
    private boolean isValidCnpj(String cnpj) {
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }
        
        try {
            // Calculate first check digit
            int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
            }
            int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
            
            // Calculate second check digit
            int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            sum = 0;
            for (int i = 0; i < 13; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
            }
            int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
            
            return Character.getNumericValue(cnpj.charAt(12)) == firstDigit &&
                   Character.getNumericValue(cnpj.charAt(13)) == secondDigit;
        } catch (Exception e) {
            return false;
        }
    }
}
