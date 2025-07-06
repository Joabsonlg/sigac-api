package io.github.joabsonlg.sigac_api.dailyRate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO usado tanto para criação quanto atualização de uma diária.
 */
public record DailyRateInputDTO(

        @NotNull(message = "O valor da diária é obrigatório")
        @Min(value = 0, message = "O valor deve ser positivo")
        Double amount,

        @NotNull(message = "A data/hora é obrigatória")
        LocalDateTime dateTime,

        @NotNull(message = "A placa do veículo é obrigatória")
        @Size(max = 20, message = "A placa deve ter no máximo 20 caracteres")
        String vehiclePlate
) {}