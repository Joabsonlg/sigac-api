package io.github.joabsonlg.sigac_api.promotion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;

import java.time.LocalDateTime;

/**
 * DTO for transferring Promotion data.
 * Contains complete promotion information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PromotionDTO(
        Integer code,
        Integer discountPercentage,
        PromotionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean isCurrentlyValid,
        Long reservationCount
) {}
