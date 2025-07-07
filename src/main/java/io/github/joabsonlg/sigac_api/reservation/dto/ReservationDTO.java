package io.github.joabsonlg.sigac_api.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;

import java.time.LocalDateTime;

/**
 * DTO for transferring Reservation data.
 * Includes complete reservation information with client and vehicle details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationDTO(
        Integer id,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime reservationDate,
        ReservationStatus status,
        Integer promotionCode,
        String clientUserCpf,
        String clientName,
        String employeeUserCpf,
        String employeeName,
        String vehiclePlate,
        String vehicleModel,
        String vehicleBrand
) {}
