package io.github.joabsonlg.sigac_api.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new Employee.
 */
public record CreateEmployeeDTO(
    @NotBlank(message = "CPF é obrigatório")
    String cpf,
    
    @NotBlank(message = "Email é obrigatório")
    String email,
    
    @NotBlank(message = "Nome é obrigatório")
    String name,
    
    @NotBlank(message = "Senha é obrigatória")
    String password,
    
    String address,
    
    String phone,
    
    @NotBlank(message = "Função é obrigatória")
    String role
) {}
