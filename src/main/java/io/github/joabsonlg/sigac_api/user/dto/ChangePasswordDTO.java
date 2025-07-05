package io.github.joabsonlg.sigac_api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for changing user password.
 */
public record ChangePasswordDTO(
    @NotBlank(message = "Senha atual é obrigatória")
    String currentPassword,
    
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Nova senha deve ter pelo menos 6 caracteres")
    String newPassword
) {}
