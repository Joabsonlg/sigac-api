package io.github.joabsonlg.sigac_api.maintenance.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import io.github.joabsonlg.sigac_api.maintenance.dto.CreateMaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.dto.UpdateMaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

/**
 * Validator para operações relacionadas a manutenção.
 * Estende funcionalidades do CommonValidator com validações específicas para manutenção.
 */
@Component
public class MaintenanceValidator {

    private final CommonValidator commonValidator;

    public MaintenanceValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }

    /**
     * Valida os dados ao criar uma manutenção.
     */
    public Mono<Void> validateCreateMaintenance(CreateMaintenanceDTO dto) {
        return Mono.fromRunnable(() -> {
            if (dto.scheduledDate() == null) {
                throw new ValidationException("Data agendada é obrigatória");
            }

            if (dto.performedDate() != null && dto.performedDate().isBefore(dto.scheduledDate())) {
                throw new ValidationException("Data realizada não pode ser anterior à data agendada");
            }

            if (isNullOrEmpty(dto.description())) {
                throw new ValidationException("Descrição da manutenção é obrigatória");
            }

            if (dto.type() == null) {
                throw new ValidationException("Tipo da manutenção é obrigatório");
            }

            if (!isNullOrEmpty(dto.cost()) && dto.cost().length() > 45) {
                throw new ValidationException("Custo da manutenção deve ter no máximo 45 caracteres");
            }

            if (isNullOrEmpty(dto.employeeUserCpf())) {
                throw new ValidationException("CPF do funcionário é obrigatório");
            }

            if (isNullOrEmpty(dto.vehiclePlate())) {
                throw new ValidationException("Placa do veículo é obrigatória");
            }
        });
    }

    /**
     * Valida os dados ao atualizar uma manutenção.
     */
    public Mono<Void> validateUpdateMaintenance(UpdateMaintenanceDTO dto) {
        return Mono.fromRunnable(() -> {
            if (dto.scheduledDate() != null && dto.performedDate() != null) {
                if (dto.performedDate().isBefore(dto.scheduledDate())) {
                    throw new ValidationException("Data realizada não pode ser anterior à data agendada");
                }
            }

            if (dto.description() != null && dto.description().length() > 255) {
                throw new ValidationException("Descrição da manutenção deve ter no máximo 255 caracteres");
            }

            if (dto.type() != null && !EnumSet.allOf(MaintenanceType.class).contains(dto.type())) {
                throw new ValidationException("Tipo da manutenção inválido");
            }

            if (dto.status() != null && !EnumSet.allOf(MaintenanceStatus.class).contains(dto.status())) {
                throw new ValidationException("Status da manutenção inválido");
            }
            if (dto.cost() != null && dto.cost().length() > 45) {
                throw new ValidationException("Custo da manutenção deve ter no máximo 45 caracteres");
            }

            if (dto.employeeUserCpf() != null && dto.employeeUserCpf().isBlank()) {
                throw new ValidationException("CPF do funcionário não pode estar vazio");
            }

            if (dto.vehiclePlate() != null && dto.vehiclePlate().isBlank()) {
                throw new ValidationException("Placa do veículo não pode estar vazia");
            }
        });
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}