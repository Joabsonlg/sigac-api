package io.github.joabsonlg.sigac_api.reservation.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod;
import io.github.joabsonlg.sigac_api.payment.repository.PaymentRepository;
import io.github.joabsonlg.sigac_api.reservation.dto.CreateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.ReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.UpdateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import io.github.joabsonlg.sigac_api.reservation.repository.ReservationRepository;
import io.github.joabsonlg.sigac_api.reservation.validator.ReservationValidator;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import io.github.joabsonlg.sigac_api.vehicle.handler.VehicleHandler;
import io.github.joabsonlg.sigac_api.dailyRate.handler.DailyRateHandler;
import io.github.joabsonlg.sigac_api.promotion.handler.PromotionHandler;
import io.github.joabsonlg.sigac_api.payment.handler.PaymentHandler;
import io.github.joabsonlg.sigac_api.payment.dto.CreatePaymentDTO;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.joabsonlg.sigac_api.reservation.dto.ReservationReportDTO;
import java.math.BigDecimal;

/**
 * Handler for business logic related to Reservation.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class ReservationHandler extends BaseHandler<Reservation, ReservationDTO, Integer> {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final VehicleHandler vehicleHandler;
    private final DailyRateHandler dailyRateHandler;
    private final PromotionHandler promotionHandler;
    private final PaymentHandler paymentHandler;
    private final PaymentRepository paymentRepository;

    public ReservationHandler(ReservationRepository reservationRepository,
                              ReservationValidator reservationValidator,
                              VehicleHandler vehicleHandler,
                              DailyRateHandler dailyRateHandler,
                              PromotionHandler promotionHandler,
                              PaymentHandler paymentHandler, PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationValidator = reservationValidator;
        this.vehicleHandler = vehicleHandler;
        this.dailyRateHandler = dailyRateHandler;
        this.promotionHandler = promotionHandler;
        this.paymentHandler = paymentHandler;
        this.paymentRepository = paymentRepository;
    }

    @Override
    protected ReservationDTO toDto(Reservation entity) {
        return toDto(entity, null, null, null, null, null);
    }

    private ReservationDTO toDto(Reservation entity, String clientName, String employeeName, String vehicleModel, String vehicleBrand, Double amount) {
        return new ReservationDTO(
            entity.id(),
            entity.startDate(),
            entity.endDate(),
            entity.reservationDate(),
            entity.status(),
            entity.promotionCode(),
            entity.clientUserCpf(),
            clientName,
            entity.employeeUserCpf(),
            employeeName,
            entity.vehiclePlate(),
            vehicleModel,
            vehicleBrand,
            amount
        );
    }

    private ReservationDTO toDto(Reservation entity, Double amount) {
        return toDto(entity, null, null, null, null, amount);
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
    private ReservationDTO arrayToReservationDto(Object[] reservationInfo, Double amount) {
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
            (String) reservationInfo[12],           // vehicleBrand
            amount
        );
    }

    /**
     * Gets all reservations with complete information
     */
    public Flux<ReservationDTO> getAll() {
        return reservationRepository.findAllWithDetails()
                .flatMap(this::mapReservationInfoToDtoWithAmount);
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
            .flatMap(this::mapReservationInfoToDtoWithAmount);

        Mono<Long> totalElements = reservationRepository.countWithFilters(status, query, cpf);

        return createPageResponse(reservations, page, size, totalElements);
    }

    private Mono<ReservationDTO> mapReservationInfoToDtoWithAmount(Object[] reservationInfo) {
        Integer promotionCode = (Integer) reservationInfo[5];
        String vehiclePlate = (String) reservationInfo[10];
        LocalDateTime startDate = (LocalDateTime) reservationInfo[1];
        LocalDateTime endDate = (LocalDateTime) reservationInfo[2];
        LocalDateTime reservationDate = (LocalDateTime) reservationInfo[3];

        Mono<Double> dailyRateMono = dailyRateHandler.getDailyRateForReservation(vehiclePlate, reservationDate)
                .map(dailyRate -> dailyRate.amount())
                .defaultIfEmpty(0.0);

        Mono<Double> discountMono = Mono.just(0.0);
        if (promotionCode != null) {
            discountMono = promotionHandler.getById(promotionCode)
                    .filter(promo -> promo.isCurrentlyValid())
                    .map(promo -> promo.discountPercentage() / 100.0)
                    .defaultIfEmpty(0.0);
        }

        return Mono.zip(dailyRateMono, discountMono)
                .map(tuple -> {
                    Double dailyRate = tuple.getT1();
                    Double discountPercentage = tuple.getT2();
                    long hours = java.time.Duration.between(startDate, endDate).toHours();
                    double calculatedAmount = (dailyRate / 24.0) * hours;
                    calculatedAmount *= (1.0 - discountPercentage);
                    return arrayToReservationDto(reservationInfo, calculatedAmount);
                });
    }

    /**
     * Creates a new reservation
     */
    @Transactional
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
                .flatMap(savedReservation -> {
                    return calculateReservationAmount(savedReservation.reservationDate(), savedReservation.startDate(), savedReservation.endDate(), savedReservation.vehiclePlate(), savedReservation.promotionCode())
                            .flatMap(amount -> {
                                CreatePaymentDTO paymentDTO = new CreatePaymentDTO(
                                    (long) savedReservation.id(),
                                    PaymentMethod.PIX,
                                    BigDecimal.valueOf(amount)
                                );
                                return paymentHandler.create(paymentDTO)
                                        .thenReturn(toDto(savedReservation, amount));
                            });
                });
    }

    /**
     * Updates an existing reservation
     */
    @Transactional
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
                .flatMap(updatedReservation -> {
                    return calculateReservationAmount(updatedReservation.reservationDate(), updatedReservation.startDate(), updatedReservation.endDate(), updatedReservation.vehiclePlate(), updatedReservation.promotionCode())
                            .map(amount -> toDto(updatedReservation, amount));
                });
    }

    public Mono<Double> calculateReservationAmount(LocalDateTime reservationDate, LocalDateTime startDate, LocalDateTime endDate, String vehiclePlate, Integer promotionCode) {
        Mono<Double> dailyRateMono = dailyRateHandler.getDailyRateForReservation(vehiclePlate, reservationDate)
                .map(dailyRate -> dailyRate.amount())
                .defaultIfEmpty(0.0);

        Mono<Double> discountMono = Mono.just(0.0);
        if (promotionCode != null) {
            discountMono = promotionHandler.getById(promotionCode)
                    .filter(promo -> promo.isCurrentlyValid())
                    .map(promo -> promo.discountPercentage() / 100.0)
                    .defaultIfEmpty(0.0);
        }

        return Mono.zip(dailyRateMono, discountMono)
                .map(tuple -> {
                    Double dailyRate = tuple.getT1();
                    Double discountPercentage = tuple.getT2();
                    long hours = java.time.Duration.between(startDate, endDate).toHours();
                    double calculatedAmount = (dailyRate / 24.0) * hours;
                    calculatedAmount *= (1.0 - discountPercentage);
                    return calculatedAmount;
                });
    }

    /**
     * Updates reservation status
     */
    @Transactional
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
    @Transactional
    public Mono<Void> delete(Integer id) {
        return paymentHandler.deleteByReservationId(id)
                .then(deleteReservationById(id));
    }

    @Transactional
    protected Mono<Void> deleteReservationById(Integer id) {
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
                .flatMap(this::mapReservationInfoToDtoWithAmount);
    }

    /**
     * Gets reservations by vehicle plate
     */
    public Flux<ReservationDTO> getByVehiclePlate(String vehiclePlate) {
        return reservationRepository.findAllWithDetails()
                .filter(data -> vehiclePlate.equals(data[10])) // vehiclePlate is at index 10
                .flatMap(this::mapReservationInfoToDtoWithAmount);
    }

    /**
     * Gets reservations by status
     */
    public Flux<ReservationDTO> getByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusWithDetails(status, 0, Integer.MAX_VALUE)
                .flatMap(this::mapReservationInfoToDtoWithAmount);
    }

    /**
     * Generates a comprehensive report of reservations.
     *
     * @return A Mono containing the ReservationReportDTO.
     */
    public Mono<ReservationReportDTO> generateReservationReport() {
        Mono<Long> totalReservationsMono = reservationRepository.countAll();
        Mono<Long> confirmedReservationsMono = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        Mono<Long> completedReservationsMono = reservationRepository.countByStatus(ReservationStatus.COMPLETED);
        Mono<Long> cancelledReservationsMono = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        Mono<BigDecimal> receitaBruta = paymentRepository.calcularReceitaBruta();

        Mono<List<ReservationDTO>> latestReservationsMono = reservationRepository.findLatestReservationsWithDetails(5)
                .flatMap(this::mapReservationInfoToDtoWithAmount)
                .collectList();

        Mono<Map<String, Double>> statusPercentagesMono = reservationRepository.countReservationsByStatus()
                .collectList()
                .flatMap(counts -> totalReservationsMono.map(total -> {
                    Map<String, Double> percentages = new HashMap<>();
                    for (Map<String, Long> countEntry : counts) {
                        String statusName = ReservationStatus.values()[countEntry.get("status").intValue()].name();
                        Double percentage = (total > 0) ? (countEntry.get("count").doubleValue() / total) * 100.0 : 0.0;
                        percentages.put(statusName, percentage);
                    }
                    return percentages;
                }));

        return Mono.zip(totalReservationsMono, confirmedReservationsMono, completedReservationsMono, cancelledReservationsMono, receitaBruta, latestReservationsMono, statusPercentagesMono)
                .map(tuple -> new ReservationReportDTO(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5(),
                        tuple.getT6(),
                        tuple.getT7()
                ));
    }
}