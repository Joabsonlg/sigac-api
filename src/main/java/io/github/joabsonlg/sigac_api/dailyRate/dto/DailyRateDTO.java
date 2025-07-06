package io.github.joabsonlg.sigac_api.dailyRate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para retorno de informações de uma diária.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DailyRateDTO(
        Long id,
        Double amount,
        LocalDateTime dateTime,
        String vehiclePlate
) {}