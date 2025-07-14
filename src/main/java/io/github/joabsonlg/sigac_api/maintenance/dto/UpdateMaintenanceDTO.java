package io.github.joabsonlg.sigac_api.maintenance.dto;

import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para atualização de manutenção.
 * O ID não pode ser alterado.
 */
public record UpdateMaintenanceDTO(

        LocalDateTime scheduledDate,

        LocalDateTime performedDate,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String description,

        @Size(max = 50, message = "O tipo deve ter no máximo 50 caracteres")
        MaintenanceType type,

        @Size(max = 50, message = "O status deve ter no máximo 50 caracteres")
        MaintenanceStatus status,

        @Size(max = 45, message = "O custo deve ter no máximo 45 caracteres")
        BigDecimal cost,

        @Size(max = 45, message = "O CPF do funcionário deve ter no máximo 45 caracteres")
        String employeeUserCpf,

        @Size(max = 45, message = "A placa do veículo deve ter no máximo 45 caracteres")
        String vehiclePlate
) {}