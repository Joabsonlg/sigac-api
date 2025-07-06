package io.github.joabsonlg.sigac_api.dailyRate.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.dailyRate.dto.DailyRateDTO;
import io.github.joabsonlg.sigac_api.dailyRate.dto.DailyRateInputDTO;
import io.github.joabsonlg.sigac_api.dailyRate.model.DailyRate;
import io.github.joabsonlg.sigac_api.dailyRate.repository.DailyRateRepository;
import io.github.joabsonlg.sigac_api.dailyRate.validator.DailyRateValidator;
import io.github.joabsonlg.sigac_api.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handler responsável pela lógica de negócio da entidade DailyRate.
 */
@Service
public class DailyRateHandler extends BaseHandler<DailyRate, DailyRateDTO, Integer> {

    private final DailyRateRepository dailyRateRepository;
    private final DailyRateValidator dailyRateValidator;
    private final VehicleRepository vehicleRepository;

    public DailyRateHandler(DailyRateRepository dailyRateRepository, DailyRateValidator dailyRateValidator, VehicleRepository vehicleRepository) {
        this.dailyRateRepository = dailyRateRepository;
        this.dailyRateValidator = dailyRateValidator;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    protected DailyRateDTO toDto(DailyRate entity) {
        return new DailyRateDTO(
                entity.id(),
                entity.amount(),
                entity.dateTime(),
                entity.vehiclePlate()
        );
    }

    @Override
    protected DailyRate toEntity(DailyRateDTO dto) {
        return new DailyRate(
                dto.id(),
                dto.amount(),
                dto.dateTime(),
                dto.vehiclePlate()
        );
    }

    public Flux<DailyRateDTO> getAll() {
        return toDtoFlux(dailyRateRepository.findAll());
    }

    public Mono<PageResponse<DailyRateDTO>> getAllPaginated(int page, int size) {
        Flux<DailyRateDTO> data = toDtoFlux(dailyRateRepository.findWithPagination(page, size));
        Mono<Long> total = dailyRateRepository.countAll();
        return createPageResponse(data, page, size, total);
    }

    public Mono<DailyRateDTO> getById(Long id) {
        return dailyRateRepository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Diária", id)));
    }

    public Mono<DailyRateDTO> create(DailyRateInputDTO dto) {
        return dailyRateValidator.validateDailyRate(dto)
                .then(vehicleRepository.existsByPlate(dto.vehiclePlate()))
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ValidationException("Veículo com placa " + dto.vehiclePlate() + " não cadastrado."));
                    }
                    DailyRate dailyRate = new DailyRate(null, dto.amount(), dto.dateTime(), dto.vehiclePlate());
                    return dailyRateRepository.save(dailyRate);
                })
                .map(this::toDto);
    }

    public Mono<DailyRateDTO> update(Long id, DailyRateInputDTO dto) {
        return dailyRateValidator.validateDailyRate(dto)
                .flatMap(v -> vehicleRepository.existsByPlate(dto.vehiclePlate()))
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ValidationException("Veículo com placa " + dto.vehiclePlate() + " não cadastrado."));
                    }
                    return dailyRateRepository.findById(id)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Diária", id)))
                            .flatMap(existing -> {
                                DailyRate updated = new DailyRate(
                                        existing.id(),
                                        dto.amount() != null ? dto.amount() : existing.amount(),
                                        dto.dateTime() != null ? dto.dateTime() : existing.dateTime(),
                                        dto.vehiclePlate() != null ? dto.vehiclePlate() : existing.vehiclePlate()
                                );
                                return dailyRateRepository.update(updated);
                            });
                })
                .map(this::toDto);
    }

    public Mono<Void> delete(Long id) {
        return dailyRateRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Diária", id)))
                .flatMap(existing -> dailyRateRepository.deleteById(String.valueOf(existing.id())));
    }
    /**
     * Busca todas as diárias de um veículo.
     *
     * @param plate placa do veículo
     * @return fluxo das diárias do veículo
     */
    public Flux<DailyRateDTO> getByVehiclePlate(String plate) {
        return dailyRateValidator.validatePlate(plate)
                .flatMapMany(validPlate -> dailyRateRepository.findByVehiclePlate(validPlate)
                        .map(this::toDto));
    }

    /**
     * Busca a diária mais recente (última cadastrada) do veículo.
     *
     * @param plate placa do veículo
     * @return diária mais recente
     */
    public Mono<DailyRateDTO> getMostRecentByVehiclePlate(String plate) {
        return dailyRateValidator.validatePlate(plate)
                .flatMap(validPlate -> dailyRateRepository.findMostRecentByVehiclePlate(validPlate)
                        .map(this::toDto));
    }

}