package io.github.joabsonlg.sigac_api.promotion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing promotion.
 * Contains updatable fields for promotion modification.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdatePromotionDTO(
        Integer discountPercentage,
        PromotionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
