package io.github.joabsonlg.sigac_api.maintenance.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.maintenance.dto.CreateMaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.dto.MaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.dto.UpdateMaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.model.Maintenance;
import io.github.joabsonlg.sigac_api.maintenance.repository.MaintenanceRepository;
import io.github.joabsonlg.sigac_api.maintenance.validator.MaintenanceValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MaintenanceHandler extends BaseHandler<Maintenance, MaintenanceDTO, Integer> {

    private final MaintenanceRepository maintenanceRepository;
    private final MaintenanceValidator maintenanceValidator;

    public MaintenanceHandler(MaintenanceRepository maintenanceRepository,
                              MaintenanceValidator maintenanceValidator) {
        this.maintenanceRepository = maintenanceRepository;
        this.maintenanceValidator = maintenanceValidator;
    }

    @Override
    protected MaintenanceDTO toDto(Maintenance entity) {
        return new MaintenanceDTO(
                entity.id(),
                entity.scheduledDate(),
                entity.performedDate(),
                entity.description(),
                entity.type(),
                entity.status(),
                entity.cost(),
                entity.employeeUserCpf() != null ? entity.employeeUserCpf() : null,
                entity.vehiclePlate() != null ? entity.vehiclePlate() : null
        );
    }

    @Override
    protected Maintenance toEntity(MaintenanceDTO dto) {
        return new Maintenance(
                dto.id(),
                dto.scheduledDate(),
                dto.performedDate(),
                dto.description(),
                dto.type(),
                dto.status(),
                dto.cost(),
                null,
                null
        );
    }

    public Flux<MaintenanceDTO> getAll() {
        return toDtoFlux(maintenanceRepository.findAll());
    }

    public Mono<MaintenanceDTO> getById(Long id) {
        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Manutenção", id)))
                .map(this::toDto);
    }

    public Mono<MaintenanceDTO> create(CreateMaintenanceDTO dto) {
        return maintenanceValidator.validateCreateMaintenance(dto)
                .then(Mono.fromCallable(() -> new Maintenance(
                        null,
                        dto.scheduledDate(),
                        dto.performedDate(),
                        dto.description(),
                        dto.type(),
                        MaintenanceStatus.AGENDADA,
                        dto.cost(),
                        null,
                        dto.vehiclePlate()
                )))
                .flatMap(maintenanceRepository::save)
                .map(this::toDto);
    }

    public Mono<MaintenanceDTO> update(Long id, UpdateMaintenanceDTO dto) {
        return maintenanceValidator.validateUpdateMaintenance(dto)
                .then(maintenanceRepository.findById(id))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Manutenção", id)))
                .flatMap(existing -> {
                    MaintenanceStatus currentStatus = existing.status();

                    // Bloquear atualização se status for CANCELADA, EM_ANDAMENTO ou CONCLUIDA
                    if (currentStatus == MaintenanceStatus.CANCELADA
                            || currentStatus == MaintenanceStatus.EM_ANDAMENTO
                            || currentStatus == MaintenanceStatus.CONCLUIDA) {
                        return Mono.error(new IllegalStateException(
                                "Não é permitido atualizar manutenção com status " + currentStatus));
                    }

                    Maintenance updated = new Maintenance(
                            existing.id(),
                            dto.scheduledDate() != null ? dto.scheduledDate() : existing.scheduledDate(),
                            dto.performedDate() != null ? dto.performedDate() : existing.performedDate(),
                            dto.description() != null ? dto.description() : existing.description(),
                            dto.type() != null ? dto.type() : existing.type(),
                            dto.status() != null ? dto.status() : existing.status(),
                            dto.cost() != null ? dto.cost() : existing.cost(),
                            existing.employeeUserCpf(),
                            existing.vehiclePlate()
                    );

                    return maintenanceRepository.update(updated);
                })
                .map(this::toDto);
    }

    public Mono<Void> delete(Long id) {
        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Manutenção", id)))
                .flatMap(maintenance -> {
                    MaintenanceStatus status = maintenance.status();

                    if (status == MaintenanceStatus.AGENDADA) {
                        Maintenance updated = new Maintenance(
                                maintenance.id(),
                                maintenance.scheduledDate(),
                                maintenance.performedDate(),
                                maintenance.description(),
                                maintenance.type(),
                                MaintenanceStatus.CANCELADA,
                                maintenance.cost(),
                                maintenance.employeeUserCpf(),
                                maintenance.vehiclePlate()
                        );
                        return maintenanceRepository.update(updated).then();
                    } else if (status == MaintenanceStatus.CONCLUIDA || status == MaintenanceStatus.EM_ANDAMENTO) {
                        return Mono.error(new IllegalStateException("Manutenção com status " + status + " não pode ser cancelada"));
                    } else {
                        return Mono.error(new IllegalStateException("Status da manutenção não permite cancelamento"));
                    }
                });
    }

}