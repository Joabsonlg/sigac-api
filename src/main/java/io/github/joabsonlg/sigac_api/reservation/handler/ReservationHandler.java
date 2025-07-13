package io.github.joabsonlg.sigac_api.reservation.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.reservation.dto.CreateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.ReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.UpdateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import io.github.joabsonlg.sigac_api.reservation.repository.ReservationRepository;
import io.github.joabsonlg.sigac_api.reservation.validator.ReservationValidator;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import io.github.joabsonlg.sigac_api.vehicle.handler.VehicleHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Handler for business logic related to Reservation.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class ReservationHandler extends BaseHandler<Reservation, ReservationDTO, Integer> {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final VehicleHandler vehicleHandler;

    public ReservationHandler(ReservationRepository reservationRepository, 
                             ReservationValidator reservationValidator, 
                             VehicleHandler vehicleHandler) {
        this.reservationRepository = reservationRepository;
        this.reservationValidator = reservationValidator;
        this.vehicleHandler = vehicleHandler;
    }

    @Override
    protected ReservationDTO toDto(Reservation entity) {
        // This method won't be used directly since we need related entity info
        return new ReservationDTO(
            entity.id(),
            entity.startDate(),
            entity.endDate(),
            entity.reservationDate(),
            entity.status(),
            entity.promotionCode(),
            entity.clientUserCpf(),
            null, // clientName - filled by methods that fetch related info
            entity.employeeUserCpf(),
            null, // employeeName - filled by methods that fetch related info
            entity.vehiclePlate(),
            null, // vehicleModel - filled by methods that fetch related info
            null  // vehicleBrand - filled by methods that fetch related info
        );
    }

    @Override
    protected Reservation toEntity(ReservationDTO dto) {
        return new Reservation(
            dto.id(),
            dto.startDate(),
            dto.endDate(),
            dto.reservationDate(),
            dto.status(),
            dto.promotionCode(),
            dto.clientUserCpf(),
            dto.employeeUserCpf(),
            dto.vehiclePlate()
        );
    }

    /**
     * Converts array of reservation info to ReservationDTO
     */
    private ReservationDTO arrayToReservationDto(Object[] reservationInfo) {
        return new ReservationDTO(
            (Integer) reservationInfo[0],           // id
            (LocalDateTime) reservationInfo[1],     // startDate
            (LocalDateTime) reservationInfo[2],     // endDate
            (LocalDateTime) reservationInfo[3],     // reservationDate
            (ReservationStatus) reservationInfo[4], // status
            (Integer) reservationInfo[5],           // promotionCode
            (String) reservationInfo[6],            // clientUserCpf
            (String) reservationInfo[7],            // clientName
            (String) reservationInfo[8],            // employeeUserCpf
            (String) reservationInfo[9],            // employeeName
            (String) reservationInfo[10],           // vehiclePlate
            (String) reservationInfo[11],           // vehicleModel
            (String) reservationInfo[12]            // vehicleBrand
        );
    }

    /**
     * Gets all reservations with complete information
     */
    public Flux<ReservationDTO> getAll() {
        return reservationRepository.findAllWithDetails()
                .map(this::arrayToReservationDto);
    }

    /**
     * Gets reservation by ID with complete information
     */
    public Mono<ReservationDTO> getById(Integer id) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Reservation", id)))
                .map(this::toDto);
    }

    /**
     * Gets paginated reservations with filtering
     */
    public Mono<PageResponse<ReservationDTO>> getAllPaginated(int page, int size,
                                                             ReservationStatus status,
                                                             String query, String cpf) {
        Flux<ReservationDTO> reservations = reservationRepository
            .findAllWithDetailsAndFilters(status, query, cpf, page, size)
            .map(this::arrayToReservationDto);

        Mono<Long> totalElements = reservationRepository.countWithFilters(status, query, cpf);

        return createPageResponse(reservations, page, size, totalElements);
    }

    /**
     * Creates a new reservation
     */
    public Mono<ReservationDTO> create(CreateReservationDTO createDto) {
        return reservationValidator.validateCreateReservation(createDto)
                .then(checkVehicleAvailability(createDto.vehiclePlate(),
                                             createDto.startDate(),
                                             createDto.endDate(),
                                             null))
                .then(Mono.fromCallable(() -> {
                    String employeeCpf = createDto.employeeUserCpf();
                    // Se não houver funcionário, deixa nulo
                    if (employeeCpf == null || employeeCpf.isBlank()) {
                        employeeCpf = null;
                    }
                    return new Reservation(
                        null,
                        createDto.startDate(),
                        createDto.endDate(),
                        LocalDateTime.now(),
                        ReservationStatus.PENDING,
                        createDto.promotionCode(),
                        createDto.clientUserCpf(),
                        employeeCpf,
                        createDto.vehiclePlate()
                    );
                }))
                .flatMap(reservationRepository::save)
                .map(this::toDto);
    }

    /**
     * Updates an existing reservation
     */
    public Mono<ReservationDTO> update(Integer id, UpdateReservationDTO updateDto) {
        return reservationValidator.validateUpdateReservation(updateDto)
                .then(reservationRepository.findById(id))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Reservation", id)))
                .flatMap(existingReservation -> {
                    // Check status transition if status is being updated
                    if (updateDto.status() != null) {
                        reservationValidator.validateStatusTransition(
                            existingReservation.status(),
                            updateDto.status()
                        );
                    }

                    // Check vehicle availability if dates or vehicle are being updated
                    LocalDateTime newStartDate = updateDto.startDate() != null ?
                        updateDto.startDate() : existingReservation.startDate();
                    LocalDateTime newEndDate = updateDto.endDate() != null ?
                        updateDto.endDate() : existingReservation.endDate();
                    String newVehiclePlate = updateDto.vehiclePlate() != null ?
                        updateDto.vehiclePlate() : existingReservation.vehiclePlate();

                    return checkVehicleAvailability(newVehiclePlate, newStartDate, newEndDate, id)
                            .then(Mono.fromCallable(() -> new Reservation(
                                existingReservation.id(),
                                newStartDate,
                                newEndDate,
                                existingReservation.reservationDate(),
                                updateDto.status() != null ? updateDto.status() : existingReservation.status(),
                                updateDto.promotionCode() != null ? updateDto.promotionCode() : existingReservation.promotionCode(),
                                existingReservation.clientUserCpf(), // Client cannot be changed
                                updateDto.employeeUserCpf() != null ? updateDto.employeeUserCpf() : existingReservation.employeeUserCpf(),
                                newVehiclePlate
                            )));
                })
                .flatMap(reservationRepository::update)
                .map(this::toDto);
    }

    /**
     * Updates reservation status
     */
    public Mono<ReservationDTO> updateStatus(Integer id, ReservationStatus newStatus) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Reservation", id)))
                .doOnNext(reservation -> reservationValidator.validateStatusTransition(
                    reservation.status(), newStatus))
                .flatMap(reservation -> {
                    ReservationStatus oldStatus = reservation.status();
                    Reservation updatedReservation = reservation.withStatus(newStatus);
                    return reservationRepository.update(updatedReservation)
                        .flatMap(savedReservation -> {
                            // Update vehicle status based on reservation status
                            VehicleStatus newVehicleStatus;
                            if (newStatus == ReservationStatus.IN_PROGRESS) {
                                newVehicleStatus = VehicleStatus.INDISPONIVEL;
                            } else {
                                newVehicleStatus = VehicleStatus.DISPONIVEL;
                            }
                            return vehicleHandler.updateVehicleStatus(savedReservation.vehiclePlate(), newVehicleStatus)
                                .thenReturn(toDto(savedReservation));
                        });
                });
    }

    /**
     * Deletes a reservation (only if status allows)
     */
    public Mono<Void> delete(Integer id) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Reservation", id)))
                .doOnNext(reservation -> {
                    if (reservation.status() == ReservationStatus.IN_PROGRESS) {
                        throw new ValidationException("Cannot delete a reservation that is in progress");
                    }
                    if (reservation.status() == ReservationStatus.COMPLETED) {
                        throw new ValidationException("Cannot delete a completed reservation");
                    }
                })
                .then(reservationRepository.deleteReservationById(id));
    }

    /**
     * Checks if a vehicle is available for the given date range
     */
    private Mono<Void> checkVehicleAvailability(String vehiclePlate,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Integer excludeReservationId) {
        return reservationRepository.isVehicleAvailable(vehiclePlate, startDate, endDate, excludeReservationId)
                .flatMap(isAvailable -> {
                    if (!isAvailable) {
                        return Mono.error(new ValidationException(
                            "Vehicle is not available for the selected date range"));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Gets reservations by client CPF
     */
    public Flux<ReservationDTO> getByClientCpf(String clientCpf) {
        return reservationRepository.findAllWithDetails()
                .filter(data -> clientCpf.equals(data[6])) // clientUserCpf is at index 6
                .map(this::arrayToReservationDto);
    }

    /**
     * Gets reservations by vehicle plate
     */
    public Flux<ReservationDTO> getByVehiclePlate(String vehiclePlate) {
        return reservationRepository.findAllWithDetails()
                .filter(data -> vehiclePlate.equals(data[10])) // vehiclePlate is at index 10
                .map(this::arrayToReservationDto);
    }

    /**
     * Gets reservations by status
     */
    public Flux<ReservationDTO> getByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusWithDetails(status, 0, Integer.MAX_VALUE)
                .map(this::arrayToReservationDto);
    }
}