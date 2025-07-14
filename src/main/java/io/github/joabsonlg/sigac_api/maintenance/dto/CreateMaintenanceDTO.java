package io.github.joabsonlg.sigac_api.maintenance.dto;

import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para criação de manutenção.
 */
public record CreateMaintenanceDTO(

        @NotNull(message = "A data agendada é obrigatória")
        LocalDateTime scheduledDate,

        LocalDateTime performedDate,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String description,

        @NotBlank(message = "O tipo é obrigatório")
        @Size(max = 50, message = "O tipo deve ter no máximo 50 caracteres")
        MaintenanceType type,

        @NotBlank(message = "O status é obrigatório")
        @Size(max = 50, message = "O status deve ter no máximo 50 caracteres")
        MaintenanceStatus status,

        @Size(max = 45, message = "O custo deve ter no máximo 45 caracteres")
        BigDecimal cost,

        @NotBlank(message = "O CPF do funcionário é obrigatório")
        @Size(max = 45, message = "O CPF do funcionário deve ter no máximo 45 caracteres")
        String employeeUserCpf,

        @NotBlank(message = "A placa do veículo é obrigatória")
        @Size(max = 45, message = "A placa do veículo deve ter no máximo 45 caracteres")
        String vehiclePlate
) {}