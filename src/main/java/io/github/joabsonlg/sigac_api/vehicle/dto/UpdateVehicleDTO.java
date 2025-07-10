package io.github.joabsonlg.sigac_api.vehicle.dto;

import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para atualização de veículo.
 * A placa não é alterável.
 */
public record UpdateVehicleDTO(

        @Size(max = 10, message = "O ano deve ter no máximo 10 caracteres")
        Integer year,

        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
        String model,

        @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres")
        String brand,

        @Size(max = 50, message = "O status deve ter no máximo 50 caracteres")
        VehicleStatus status,

        @Size(max = 255, message = "A URL da imagem deve ter no máximo 255 caracteres")
        String imageUrl,

        @NotNull(message = "O valor da diária é obrigatório")
        @Min(value = 0, message = "O valor deve ser positivo")
        Double dailyRate
) {
}
