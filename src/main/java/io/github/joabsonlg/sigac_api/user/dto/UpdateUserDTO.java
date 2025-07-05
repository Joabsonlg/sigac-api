package io.github.joabsonlg.sigac_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating User information.
 * Excludes CPF (immutable) and password (updated separately).
 */
public record UpdateUserDTO(
    @Email(message = "Email deve ter formato válido")
    String email,
    
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    String name,
    
    String address,
    
    String phone
) {}
