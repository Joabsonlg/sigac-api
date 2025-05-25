package io.github.joabsonlg.sigac_api.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for common string operations.
 * Provides helper methods for string manipulation and formatting.
 */
public class StringUtil {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Checks if a string is null or empty.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not null and not empty.
     */
    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }
    
    /**
     * Capitalizes the first letter of a string.
     */
    public static String capitalize(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Removes all non-numeric characters from a string.
     */
    public static String removeNonNumeric(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.replaceAll("[^\\d]", "");
    }
    
    /**
     * Formats a CPF string with dots and dash.
     */
    public static String formatCpf(String cpf) {
        String cleanCpf = removeNonNumeric(cpf);
        if (cleanCpf.length() != 11) {
            return cpf;
        }
        return String.format("%s.%s.%s-%s",
                cleanCpf.substring(0, 3),
                cleanCpf.substring(3, 6),
                cleanCpf.substring(6, 9),
                cleanCpf.substring(9, 11));
    }
    
    /**
     * Formats a CNPJ string with dots, slash and dash.
     */
    public static String formatCnpj(String cnpj) {
        String cleanCnpj = removeNonNumeric(cnpj);
        if (cleanCnpj.length() != 14) {
            return cnpj;
        }
        return String.format("%s.%s.%s/%s-%s",
                cleanCnpj.substring(0, 2),
                cleanCnpj.substring(2, 5),
                cleanCnpj.substring(5, 8),
                cleanCnpj.substring(8, 12),
                cleanCnpj.substring(12, 14));
    }
    
    /**
     * Generates a random UUID string.
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Formats a LocalDateTime to string.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    /**
     * Truncates a string to a maximum length.
     */
    public static String truncate(String str, int maxLength) {
        if (isNullOrEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
    
    /**
     * Masks sensitive data (e.g., email, CPF) for logging.
     */
    public static String maskSensitiveData(String data, int visibleChars) {
        if (isNullOrEmpty(data) || data.length() <= visibleChars) {
            return "*".repeat(data != null ? data.length() : 0);
        }
        
        String visible = data.substring(0, visibleChars);
        String masked = "*".repeat(data.length() - visibleChars);
        return visible + masked;
    }
}
