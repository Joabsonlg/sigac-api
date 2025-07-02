package io.github.joabsonlg.sigac_api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes.
 * Used to generate hashes for initial data in start.sql.
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Passwords from start.sql
        String[] passwords = {
            "admin123",   // João Silva
            "func123",    // Ana Santos  
            "cli123",     // Carlos Oliveira
            "cli456",     // Maria Costa
            "cli789",     // Pedro Almeida
            "func456",    // Lúcia Ferreira
            "cli321",     // José Rodrigues
            "cli654"      // Fernanda Lima
        };
        
        String[] names = {
            "João Silva (ADMIN)",
            "Ana Santos (ATENDENTE)",
            "Carlos Oliveira (CLIENTE)",
            "Maria Costa (CLIENTE)",
            "Pedro Almeida (CLIENTE)",
            "Lúcia Ferreira (GERENTE)",
            "José Rodrigues (CLIENTE)",
            "Fernanda Lima (CLIENTE)"
        };
        
        System.out.println("-- BCrypt password hashes for start.sql");
        System.out.println("-- Copy these values to replace plain text passwords");
        System.out.println();
        
        for (int i = 0; i < passwords.length; i++) {
            String hash = encoder.encode(passwords[i]);
            System.out.println("-- " + names[i] + " (password: " + passwords[i] + "):");
            System.out.println("-- '" + hash + "'");
            System.out.println();
        }
    }
}
