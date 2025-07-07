package io.github.joabsonlg.sigac_api.promotion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO for creating a new promotion.
 * Contains all required fields for promotion creation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreatePromotionDTO(
        Integer discountPercentage,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
