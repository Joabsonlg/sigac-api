package io.github.joabsonlg.sigac_api.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for password operations.
 * Handles password encoding, validation, and security operations.
 */
@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }
    
    /**
     * Encodes a raw password.
     *
     * @param rawPassword the raw password to encode
     * @return encoded password
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Verifies if a raw password matches an encoded password.
     *
     * @param rawPassword     the raw password
     * @param encodedPassword the encoded password
     * @return true if passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Validates password strength.
     *
     * @param password password to validate
     * @return true if password meets requirements, false otherwise
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one digit, one lowercase, one uppercase, and one special character
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        return hasDigit && hasLower && hasUpper && hasSpecial;
    }
    
    /**
     * Gets password requirements description.
     *
     * @return password requirements as string
     */
    public String getPasswordRequirements() {
        return "Password must be at least 8 characters long and contain at least one digit, " +
               "one lowercase letter, one uppercase letter, and one special character";
    }
}
