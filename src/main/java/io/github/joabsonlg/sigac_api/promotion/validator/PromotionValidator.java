package io.github.joabsonlg.sigac_api.promotion.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import io.github.joabsonlg.sigac_api.promotion.dto.CreatePromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.dto.UpdatePromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Validator for promotion-related operations.
 * Extends CommonValidator functionality with promotion-specific validations.
 */
@Component
public class PromotionValidator {

    private final CommonValidator commonValidator;

    public PromotionValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }

    /**
     * Validates data when creating a promotion.
     */
    public Mono<Void> validateCreatePromotion(CreatePromotionDTO dto) {
        return Mono.fromRunnable(() -> {
            validateRequired(dto.discountPercentage(), "Discount percentage");
            validateRequired(dto.startDate(), "Start date");
            validateRequired(dto.endDate(), "End date");
            
            // Validate discount percentage range
            validateDiscountPercentage(dto.discountPercentage());
            
            // Validate date logic
            validateDateRange(dto.startDate(), dto.endDate());
        });
    }

    /**
     * Validates data when updating a promotion.
     */
    public Mono<Void> validateUpdatePromotion(UpdatePromotionDTO dto) {
        return Mono.fromRunnable(() -> {
            if (dto.discountPercentage() != null) {
                validateDiscountPercentage(dto.discountPercentage());
            }
            
            if (dto.startDate() != null && dto.endDate() != null) {
                validateDateRange(dto.startDate(), dto.endDate());
            }
        });
    }

    /**
     * Validates that discount percentage is within valid range.
     */
    private void validateDiscountPercentage(Integer discountPercentage) {
        if (discountPercentage == null) {
            throw new ValidationException("Discount percentage is required");
        }
        
        if (discountPercentage < 1 || discountPercentage > 100) {
            throw new ValidationException("Discount percentage must be between 1 and 100");
        }
    }

    /**
     * Validates that the promotion dates are logical.
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("End date cannot be in the past");
        }
        
        // Allow promotions to start in the past (for immediate activation)
        // but warn if it's too far in the past
        if (startDate.isBefore(LocalDateTime.now().minusDays(30))) {
            throw new ValidationException("Start date cannot be more than 30 days in the past");
        }
    }

    /**
     * Validates that status transition is allowed.
     */
    public void validateStatusTransition(PromotionStatus currentStatus, PromotionStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }
        
        // Define allowed transitions
        boolean isValidTransition = switch (currentStatus) {
            case SCHEDULED -> newStatus == PromotionStatus.ACTIVE || 
                            newStatus == PromotionStatus.INACTIVE;
            case ACTIVE -> newStatus == PromotionStatus.INACTIVE;
            case INACTIVE -> newStatus == PromotionStatus.ACTIVE || 
                           newStatus == PromotionStatus.SCHEDULED;
        };
        
        if (!isValidTransition) {
            throw new ValidationException(
                String.format("Invalid status transition from %s to %s", 
                             currentStatus, newStatus)
            );
        }
    }

    /**
     * Validates that a promotion can be deleted.
     */
    public void validateDeletion(PromotionStatus status, long reservationCount) {
        if (status == PromotionStatus.ACTIVE) {
            throw new ValidationException("Cannot delete an active promotion");
        }
        
        if (reservationCount > 0) {
            throw new ValidationException(
                String.format("Cannot delete promotion that has been used in %d reservation(s)", 
                             reservationCount)
            );
        }
    }

    /**
     * Validates promotion activation based on dates.
     */
    public void validateActivation(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(startDate)) {
            throw new ValidationException("Cannot activate promotion before its start date");
        }
        
        if (now.isAfter(endDate)) {
            throw new ValidationException("Cannot activate promotion after its end date");
        }
    }

    /**
     * Validates promotion deactivation.
     */
    public void validateDeactivation(PromotionStatus currentStatus) {
        if (currentStatus != PromotionStatus.ACTIVE) {
            throw new ValidationException("Can only deactivate active promotions");
        }
    }

    /**
     * Validates that promotion dates don't overlap with other active promotions (if business rule applies).
     */
    public void validateNoOverlap(LocalDateTime startDate, LocalDateTime endDate, Integer excludePromotionCode) {
        // This method can be enhanced if business rules require no overlapping active promotions
        // For now, we allow multiple active promotions at the same time
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
