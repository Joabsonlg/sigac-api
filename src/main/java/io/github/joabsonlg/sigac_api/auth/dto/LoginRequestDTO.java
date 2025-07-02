package io.github.joabsonlg.sigac_api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for login request.
 * Contains user credentials for authentication.
 */
@Schema(description = "Dados para autenticação do usuário")
public record LoginRequestDTO(
        @JsonProperty("cpf")
        @Schema(description = "CPF do usuário (apenas números)", example = "36900271014", required = true)
        String cpf,
        
        @JsonProperty("password")
        @Schema(description = "Senha do usuário", example = "admin123", required = true)
        String password
) {
    /**
     * Constructor for creating login request.
     *
     * @param cpf      user's CPF
     * @param password user's password
     */
    public LoginRequestDTO {
        // Validation will be handled by the handler using CommonValidator
    }
}
