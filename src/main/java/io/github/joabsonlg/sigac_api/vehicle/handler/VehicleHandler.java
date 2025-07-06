package io.github.joabsonlg.sigac_api.vehicle.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ConflictException;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.vehicle.dto.CreateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.UpdateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.VehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;
import io.github.joabsonlg.sigac_api.vehicle.repository.VehicleRepository;
import io.github.joabsonlg.sigac_api.vehicle.validator.VehicleValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handler para lógica de negócio relacionada a Veículo.
 * Extende BaseHandler para conversão entre entidade e DTO.
 */
@Service
public class VehicleHandler extends BaseHandler<Vehicle, VehicleDTO, String> {

    private final VehicleRepository vehicleRepository;
    private final VehicleValidator vehicleValidator;

    public VehicleHandler(VehicleRepository vehicleRepository, VehicleValidator vehicleValidator) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleValidator = vehicleValidator;
    }

    @Override
    protected VehicleDTO toDto(Vehicle entity) {
        return new VehicleDTO(
                entity.plate(),
                entity.year(),
                entity.model(),
                entity.brand(),
                entity.status(),
                entity.imageUrl()
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
    public Flux<VehicleDTO> getAll() {
        return toDtoFlux(vehicleRepository.findAll());
    }

    /**
     * Retorna veículos paginados.
     *
     * @param page página atual
     * @param size tamanho da página
     * @return Mono com resposta paginada
     */
    public Mono<PageResponse<VehicleDTO>> getAllPaginated(int page, int size) {
        Flux<VehicleDTO> vehicles = toDtoFlux(vehicleRepository.findWithPagination(page, size));
        Mono<Long> totalElements = vehicleRepository.countAll();

        return createPageResponse(vehicles, page, size, totalElements);
    }

    /**
     * Busca veículo pelo número da placa.
     *
     * @param plate placa do veículo
     * @return Mono com DTO do veículo encontrado
     */
    public Mono<VehicleDTO> getById(String plate) {
        return vehicleRepository.findById(plate)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Veículo", plate)));
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
                .map(this::toDto);
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
                            existing.plate(), // não muda
                            dto.year() != null ? dto.year() : existing.year(),
                            dto.model() != null ? dto.model() : existing.model(),
                            dto.brand() != null ? dto.brand() : existing.brand(),
                            dto.status() != null ? dto.status() : existing.status(),
                            dto.imageUrl() != null ? dto.imageUrl() : existing.imageUrl()
                    );
                    return vehicleRepository.update(updated);
                })
                .map(this::toDto);
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
                    return vehicleRepository.deleteByPlate(plate);
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