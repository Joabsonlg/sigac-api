package io.github.joabsonlg.sigac_api.maintenance.dto;

import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MaintenanceStatusUpdateDTO(
        @NotNull MaintenanceStatus status,
        BigDecimal cost
) {}