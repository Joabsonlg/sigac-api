package io.github.joabsonlg.sigac_api.vehicle.dto;

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
        String status,

        @Size(max = 255, message = "A URL da imagem deve ter no máximo 255 caracteres")
        String imageUrl
) {}
