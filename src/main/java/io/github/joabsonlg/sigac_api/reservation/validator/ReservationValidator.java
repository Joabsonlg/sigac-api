package io.github.joabsonlg.sigac_api.reservation.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import io.github.joabsonlg.sigac_api.reservation.dto.CreateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.UpdateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Validator for reservation-related operations.
 * Extends CommonValidator functionality with reservation-specific validations.
 */
@Component
public class ReservationValidator {

    private final CommonValidator commonValidator;

    public ReservationValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }

    /**
     * Validates data when creating a reservation.
     */
    public Mono<Void> validateCreateReservation(CreateReservationDTO dto) {
        return Mono.fromRunnable(() -> {
            validateRequired(dto.startDate(), "Start date");
            validateRequired(dto.endDate(), "End date");
            validateRequired(dto.clientUserCpf(), "Client CPF");
            validateRequired(dto.vehiclePlate(), "Vehicle plate");
            
            // Validate CPF format
            commonValidator.validateCpf(dto.clientUserCpf(), "Client CPF");
            
            // Validate date logic
            validateDateRange(dto.startDate(), dto.endDate());
            
            // Validate vehicle plate format
            validateVehiclePlate(dto.vehiclePlate());
        });
    }

    /**
     * Validates data when updating a reservation.
     */
    public Mono<Void> validateUpdateReservation(UpdateReservationDTO dto) {
        return Mono.fromRunnable(() -> {
            if (dto.startDate() != null && dto.endDate() != null) {
                validateDateRange(dto.startDate(), dto.endDate());
            }
            
            if (dto.employeeUserCpf() != null) {
                commonValidator.validateCpf(dto.employeeUserCpf(), "Employee CPF");
            }
            
            if (dto.vehiclePlate() != null) {
                validateVehiclePlate(dto.vehiclePlate());
            }
        });
    }

    /**
     * Validates that the reservation dates are logical.
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        
        if (startDate.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new ValidationException("Start date cannot be in the past");
        }
        
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("End date cannot be in the past");
        }
    }

    /**
     * Validates vehicle plate format.
     */
    private void validateVehiclePlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            throw new ValidationException("Vehicle plate is required");
        }
        
        // Basic plate validation (can be enhanced based on requirements)
        if (plate.length() < 7 || plate.length() > 8) {
            throw new ValidationException("Invalid vehicle plate format");
        }
    }

    /**
     * Validates that status transition is allowed.
     */
    public void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }
        
        // Define allowed transitions
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == ReservationStatus.CONFIRMED || 
                          newStatus == ReservationStatus.CANCELLED;
            case CONFIRMED -> newStatus == ReservationStatus.IN_PROGRESS || 
                             newStatus == ReservationStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == ReservationStatus.COMPLETED || 
                               newStatus == ReservationStatus.CANCELLED;
            case COMPLETED -> false; // Completed reservations cannot be changed
            case CANCELLED -> false; // Cancelled reservations cannot be changed
        };
        
        if (!isValidTransition) {
            throw new ValidationException(
                String.format("Invalid status transition from %s to %s", 
                             currentStatus, newStatus)
            );
        }
    }

    /**
     * Validates required field.
     */
    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (value instanceof String str && str.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
}
