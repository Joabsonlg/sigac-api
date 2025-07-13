package io.github.joabsonlg.sigac_api.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.maintenance.dto.MaintenanceDTO;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehicleReportDTO(
    Long totalVehicles,
    Long availableVehicles,
    Long inUseVehicles,
    Long inMaintenanceVehicles,
    Map<String, Double> statusPercentages,
    List<MaintenanceDTO> latestMaintenances
) {}
