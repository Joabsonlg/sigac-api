package io.github.joabsonlg.sigac_api.vehicle.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ConflictException;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.response.ErrorResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.dailyRate.dto.DailyRateInputDTO;
import io.github.joabsonlg.sigac_api.dailyRate.handler.DailyRateHandler;
import io.github.joabsonlg.sigac_api.dailyRate.repository.DailyRateRepository;
import io.github.joabsonlg.sigac_api.reservation.repository.ReservationRepository;
import io.github.joabsonlg.sigac_api.vehicle.dto.CreateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.UpdateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.VehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.VehicleReportDTO;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;
import io.github.joabsonlg.sigac_api.vehicle.repository.VehicleRepository;
import io.github.joabsonlg.sigac_api.vehicle.validator.VehicleValidator;
import io.github.joabsonlg.sigac_api.maintenance.dto.MaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import io.github.joabsonlg.sigac_api.maintenance.dto.MaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import io.github.joabsonlg.sigac_api.maintenance.repository.MaintenanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler para lógica de negócio relacionada a Veículo.
 * Extende BaseHandler para conversão entre entidade e DTO.
 */
@Service
public class VehicleHandler extends BaseHandler<Vehicle, VehicleDTO, String> {

    private final VehicleRepository vehicleRepository;
    private final VehicleValidator vehicleValidator;
    private final DailyRateHandler dailyRateHandler;
    private final MaintenanceRepository maintenanceRepository;
    private final ReservationRepository reservationRepository;
    private final DailyRateRepository dailyRateRepository;

    public VehicleHandler(VehicleRepository vehicleRepository, VehicleValidator vehicleValidator,
                          DailyRateHandler dailyRateHandler, MaintenanceRepository maintenanceRepository,
                          ReservationRepository reservationRepository, DailyRateRepository dailyRateRepository) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleValidator = vehicleValidator;
        this.dailyRateHandler = dailyRateHandler;
        this.maintenanceRepository = maintenanceRepository;
        this.reservationRepository = reservationRepository;
        this.dailyRateRepository = dailyRateRepository;
    }
    @Override
    protected VehicleDTO toDto(Vehicle entity) {
        return toDto(entity, null);
    }

    private VehicleDTO toDto(Vehicle entity, Double amount) {
        return new VehicleDTO(
                entity.plate(),
                entity.year(),
                entity.model(),
                entity.brand(),
                entity.status(),
                entity.imageUrl(),
                amount
        );
    }

    @Override
    protected Vehicle toEntity(VehicleDTO dto) {
        return new Vehicle(
                dto.plate(),
                dto.year(),
                dto.model(),
                dto.brand(),
                dto.status(),
                dto.imageUrl()
        );
    }

    /**
     * Retorna todos os veículos.
     *
     * @return Flux com DTOs de veículos
     */
    public Flux<VehicleDTO> getAll(VehicleStatus status) {
        return vehicleRepository.findAll(status)
                .flatMap(vehicle ->
                        dailyRateHandler.getMostRecentByVehiclePlate(vehicle.plate())
                                .map(dailyRate -> toDto(vehicle, dailyRate.amount()))
                                .defaultIfEmpty(toDto(vehicle, null))
                );
    }

    /**
     * Retorna veículos paginados.
     *
     * @param page página atual
     * @param size tamanho da página
     * @return Mono com resposta paginada
     */
    public Mono<PageResponse<VehicleDTO>> getAllPaginated(int page, int size, VehicleStatus status) {
        Flux<VehicleDTO> vehiclesWithAmount = vehicleRepository.findWithPagination(page, size, status)
                .flatMap(vehicle ->
                        dailyRateHandler.getMostRecentByVehiclePlate(vehicle.plate())
                                .map(dailyRate -> toDto(vehicle, dailyRate.amount()))
                                .defaultIfEmpty(toDto(vehicle, null))
                );

        Mono<Long> totalElements = vehicleRepository.countAll(status);

        return createPageResponse(vehiclesWithAmount, page, size, totalElements);
    }

    /**
     * Generates a comprehensive report of vehicles.
     *
     * @return A Mono containing the VehicleReportDTO.
     */
    public Mono<VehicleReportDTO> generateVehicleReport() {
        Mono<Long> totalVehiclesMono = vehicleRepository.countAll(null);
        Mono<Long> availableVehiclesMono = vehicleRepository.countAll(VehicleStatus.DISPONIVEL);
        Mono<Long> inUseVehiclesMono = vehicleRepository.countAll(VehicleStatus.ALUGADO);
        Mono<Long> inMaintenanceVehiclesMono = vehicleRepository.countAll(VehicleStatus.MANUTENCAO);

        Mono<Map<String, Double>> statusPercentagesMono = vehicleRepository.countVehiclesByStatus()
                .collectList()
                .flatMap(counts -> totalVehiclesMono.map(total -> {
                    Map<String, Double> percentages = new HashMap<>();
                    for (Map<String, Long> countEntry : counts) {
                        String statusName = VehicleStatus.values()[countEntry.get("status").intValue()].name();
                        Double percentage = (total > 0) ? (countEntry.get("count").doubleValue() / total) * 100.0 : 0.0;
                        percentages.put(statusName, percentage);
                    }
                    return percentages;
                }));

        Mono<List<MaintenanceDTO>> latestMaintenancesMono = maintenanceRepository.findLatestMaintenancesWithDetails(5)
                .map(this::mapMaintenanceInfoToDto)
                .collectList();

        return Mono.zip(totalVehiclesMono, availableVehiclesMono, inUseVehiclesMono, inMaintenanceVehiclesMono, statusPercentagesMono, latestMaintenancesMono)
                .map(tuple -> new VehicleReportDTO(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5(),
                        tuple.getT6()
                ));
    }

    private MaintenanceDTO mapMaintenanceInfoToDto(Object[] maintenanceInfo) {
        return new MaintenanceDTO(
            (Long) maintenanceInfo[0],           // id
            (LocalDateTime) maintenanceInfo[1],  // scheduledDate
            (LocalDateTime) maintenanceInfo[2],  // performedDate
            (String) maintenanceInfo[3],         // description
            (MaintenanceType) maintenanceInfo[4], // type
            (MaintenanceStatus) maintenanceInfo[5], // status
            (BigDecimal) maintenanceInfo[6],         // cost
            (String) maintenanceInfo[7],         // employeeUserCpf
            (String) maintenanceInfo[8],         // employeeName
            (String) maintenanceInfo[9],         // vehiclePlate
            (String) maintenanceInfo[10],        // vehicleModel
            (String) maintenanceInfo[11]         // vehicleBrand
        );
    }

    /**
     * Busca veículo pelo número da placa.
     *
     * @param plate placa do veículo
     * @return Mono com DTO do veículo encontrado
     */
    public Mono<VehicleDTO> getById(String plate) {
        return vehicleRepository.findById(plate)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Veículo", plate)))
                .flatMap(vehicle ->
                        dailyRateHandler.getMostRecentByVehiclePlate(plate)
                                .map(dailyRate -> toDto(vehicle, dailyRate.amount()))
                                .defaultIfEmpty(toDto(vehicle, null))
                );
    }

    /**
     * Cria um novo veículo.
     *
     * @param createVehicleDTO DTO com dados para criação
     * @return Mono com DTO do veículo criado
     */
    public Mono<VehicleDTO> create(CreateVehicleDTO createVehicleDTO) {
        return vehicleValidator.validateCreateVehicle(createVehicleDTO)
                .then(checkIfPlateExists(createVehicleDTO.plate()))
                .then(Mono.fromCallable(() -> new Vehicle(
                        createVehicleDTO.plate(),
                        createVehicleDTO.year(),
                        createVehicleDTO.model(),
                        createVehicleDTO.brand(),
                        createVehicleDTO.status(),
                        createVehicleDTO.imageUrl()
                )))
                .flatMap(vehicleRepository::save)
                .flatMap(savedVehicle -> {
                    DailyRateInputDTO rateDTO = new DailyRateInputDTO(
                            createVehicleDTO.dailyRate(),
                            LocalDateTime.now(),
                            savedVehicle.plate()
                    );
                    return dailyRateHandler.create(rateDTO)
                            .thenReturn(toDto(savedVehicle));
                });
    }

    /**
     * Atualiza um veículo existente.
     *
     * @param plate placa do veículo
     * @param dto DTO com dados para atualização
     * @return Mono com DTO do veículo atualizado
     */
    public Mono<VehicleDTO> update(String plate, UpdateVehicleDTO dto) {
        return vehicleValidator.validateUpdateVehicle(dto)
                .then(vehicleRepository.findById(plate))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Veículo", plate)))
                .flatMap(existing -> {
                    Vehicle updated = new Vehicle(
                            existing.plate(),
                            dto.year() != null ? dto.year() : existing.year(),
                            dto.model() != null ? dto.model() : existing.model(),
                            dto.brand() != null ? dto.brand() : existing.brand(),
                            dto.status() != null ? dto.status() : existing.status(),
                            dto.imageUrl() != null ? dto.imageUrl() : existing.imageUrl()
                    );
                    return vehicleRepository.update(updated)
                            .flatMap(saved -> {
                                DailyRateInputDTO rateDTO = new DailyRateInputDTO(
                                        dto.dailyRate(),
                                        LocalDateTime.now(),
                                        plate
                                );
                                return dailyRateHandler.create(rateDTO)
                                        .thenReturn(toDto(saved));
                            });
                });
    }

    /**
     * Exclui um veículo pelo número da placa.
     *
     * @param plate placa do veículo
     * @return Mono vazio ao finalizar
     */
    public Mono<Void> delete(String plate) {
        return vehicleRepository.existsByPlate(plate)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Veículo", plate));
                    }

                    // Primeiro: verificar se tem reserva
                    return reservationRepository.existsByVehiclePlate(plate)
                            .flatMap(hasReservation -> {
                                if (hasReservation) {
                                    return Mono.error(new ConflictException("Veículo com placa " + plate + " está associado a uma reserva"));
                                }

                                // Segundo: verificar se tem diária
                                return dailyRateRepository.existsByVehiclePlate(plate)
                                        .flatMap(hasDailyRate -> {
                                            if (hasDailyRate) {
                                                return Mono.error(new ConflictException("Veículo com placa " + plate + " está associado a uma diária"));
                                            }

                                            // Tudo certo: deletar
                                            return vehicleRepository.deleteByPlate(plate);
                                        });
                            });
                });
    }

    /**
     * Verifica se veículo existe pela placa.
     *
     * @param plate placa a verificar
     * @return Mono booleano indicando existência
     */
    public Mono<Boolean> existsByPlate(String plate) {
        return vehicleRepository.existsByPlate(plate);
    }

    /**
     * Atualiza o status de um veículo.
     *
     * @param plate placa do veículo
     * @param newStatus novo status do veículo
     * @return Mono vazio ao finalizar
     */
    public Mono<Void> updateVehicleStatus(String plate, VehicleStatus newStatus) {
        return vehicleRepository.findById(plate)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Veículo", plate)))
                .flatMap(existingVehicle -> {
                    Vehicle updatedVehicle = new Vehicle(
                            existingVehicle.plate(),
                            existingVehicle.year(),
                            existingVehicle.model(),
                            existingVehicle.brand(),
                            newStatus,
                            existingVehicle.imageUrl()
                    );
                    return vehicleRepository.update(updatedVehicle).then();
                });
    }

    /**
     * Helper para verificar se a placa já existe antes de criar novo veículo.
     *
     * @param plate placa a verificar
     * @return Mono vazio se não existir, erro se existir
     */
    private Mono<Void> checkIfPlateExists(String plate) {
        return vehicleRepository.existsByPlate(plate)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Veículo com placa " + plate + " já existe"));
                    }
                    return Mono.empty();
                });
    }
}
