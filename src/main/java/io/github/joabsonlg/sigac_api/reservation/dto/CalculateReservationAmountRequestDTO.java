package io.github.joabsonlg.sigac_api.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalculateReservationAmountRequestDTO(
    @NotNull(message = "A data de início é obrigatória")
    LocalDateTime startDate,

    @NotNull(message = "A data de fim é obrigatória")
    LocalDateTime endDate,

    @NotBlank(message = "A placa do veículo é obrigatória")
    String vehiclePlate,

    Integer promotionCode
) {}
