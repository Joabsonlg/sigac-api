package io.github.joabsonlg.sigac_api.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing reservation.
 * Contains updatable fields for reservation modification.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateReservationDTO(
        LocalDateTime startDate,
        LocalDateTime endDate,
        ReservationStatus status,
        Integer promotionCode,
        String employeeUserCpf,
        String vehiclePlate
) {}
