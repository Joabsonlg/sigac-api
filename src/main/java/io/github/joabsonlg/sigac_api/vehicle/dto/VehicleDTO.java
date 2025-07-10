package io.github.joabsonlg.sigac_api.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;

/**
 * DTO para retorno de dados de veículos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehicleDTO(
        String plate,
        Integer year,
        String model,
        String brand,
        VehicleStatus status,
        String imageUrl,
        Double dailyRate
) {}