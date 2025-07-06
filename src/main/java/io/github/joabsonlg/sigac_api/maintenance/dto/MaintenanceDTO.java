package io.github.joabsonlg.sigac_api.maintenance.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;

import java.time.LocalDateTime;

/**
 * DTO para retorno dos dados de manutenção.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MaintenanceDTO(

        Long id,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime scheduledDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime performedDate,

        String description,

        MaintenanceType type,

        MaintenanceStatus status,

        String cost,

        String employeeUserCpf,

        String vehiclePlate
) {}