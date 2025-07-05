package io.github.joabsonlg.sigac_api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for transferring Client data with complete user information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientDTO(
    String cpf,
    String email,
    String name,
    String address,
    String phone
) {
    /**
     * Creates a ClientDTO from UserDTO
     */
    public static ClientDTO fromUserDTO(UserDTO userDTO) {
        return new ClientDTO(
            userDTO.cpf(),
            userDTO.email(),
            userDTO.name(),
            userDTO.address(),
            userDTO.phone()
        );
    }
}
