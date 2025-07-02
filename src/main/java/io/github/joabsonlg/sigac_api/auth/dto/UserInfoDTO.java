package io.github.joabsonlg.sigac_api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user information in authentication responses.
 * Contains basic user data without sensitive information.
 */
@Schema(description = "Informações do usuário autenticado")
public record UserInfoDTO(
        @JsonProperty("cpf")
        @Schema(description = "CPF do usuário", example = "36900271014")
        String cpf,
        
        @JsonProperty("name")
        @Schema(description = "Nome completo do usuário", example = "João Silva")
        String name,
        
        @JsonProperty("email")
        @Schema(description = "Email do usuário", example = "admin@sigac.com")
        String email,
        
        @JsonProperty("role")
        @Schema(description = "Papel do usuário no sistema", example = "ADMIN", allowableValues = {"ADMIN", "GERENTE", "ATENDENTE", "CLIENTE"})
        String role
) {
    /**
     * Constructor for creating user info.
     *
     * @param cpf   user's CPF
     * @param name  user's name
     * @param email user's email
     * @param role  user's role (CLIENT, EMPLOYEE, ADMIN, etc.)
     */
    public UserInfoDTO {
        // No additional validation needed here
    }
}
