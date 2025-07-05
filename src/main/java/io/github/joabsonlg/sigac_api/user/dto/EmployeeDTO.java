package io.github.joabsonlg.sigac_api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for transferring Employee data with complete user information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeDTO(
    String cpf,
    String email,
    String name,
    String address,
    String phone,
    String role
) {
    /**
     * Creates an EmployeeDTO from UserDTO and role
     */
    public static EmployeeDTO fromUserDTO(UserDTO userDTO, String role) {
        return new EmployeeDTO(
            userDTO.cpf(),
            userDTO.email(),
            userDTO.name(),
            userDTO.address(),
            userDTO.phone(),
            role
        );
    }
}
