package io.github.joabsonlg.sigac_api.maintenance.dto;

import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

public record MaintenanceStatusUpdateDTO(
        @NotNull MaintenanceStatus status
) {}
