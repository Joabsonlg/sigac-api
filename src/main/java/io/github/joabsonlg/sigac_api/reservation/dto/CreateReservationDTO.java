package io.github.joabsonlg.sigac_api.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO for creating a new reservation.
 * Contains all required fields for reservation creation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateReservationDTO(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer promotionCode,
        String clientUserCpf,
        String employeeUserCpf,
        String vehiclePlate
) {}
