package io.github.joabsonlg.sigac_api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for transferring User data.
 * Excludes sensitive information like password.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(
    String cpf,
    String email,
    String name,
    String address,
    String phone
) {}
