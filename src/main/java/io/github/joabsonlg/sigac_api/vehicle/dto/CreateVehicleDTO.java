package io.github.joabsonlg.sigac_api.vehicle.dto;

import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para criação de veículo.
 */
public record CreateVehicleDTO(
        @NotBlank(message = "A placa é obrigatória")
        @Size(max = 20, message = "A placa deve ter no máximo 20 caracteres")
        String plate,

        @NotBlank(message = "O ano é obrigatório")
        @Min(value = 1900, message = "O ano mínimo é 1900")
        @Max(value = 2100, message = "O ano máximo é 2100")
        Integer year,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
        String model,

        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres")
        String brand,

        @NotBlank(message = "O status é obrigatório")
        @Size(max = 50, message = "O status deve ter no máximo 50 caracteres")
        VehicleStatus status,

        @Size(max = 255, message = "A URL da imagem deve ter no máximo 255 caracteres")
        String imageUrl
) {}