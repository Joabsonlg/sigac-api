package io.github.joabsonlg.sigac_api.reservation.controller;

import io.github.joabsonlg.sigac_api.auth.dto.UserInfoDTO;
import io.github.joabsonlg.sigac_api.auth.handler.AuthHandler;
import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.reservation.dto.CreateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.ReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.UpdateReservationDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.CalculateReservationAmountRequestDTO;
import io.github.joabsonlg.sigac_api.reservation.dto.ReservationReportDTO;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import io.github.joabsonlg.sigac_api.reservation.handler.ReservationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Controller that exposes REST endpoints for Reservation management.
 * Extends BaseController for standardized HTTP responses.
 */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation", description = "Operations related to reservation management")
public class ReservationController extends BaseController<ReservationDTO, Integer> {

    private final ReservationHandler reservationHandler;
    private final AuthHandler authHandler;

    public ReservationController(ReservationHandler reservationHandler, AuthHandler authHandler) {
        this.reservationHandler = reservationHandler;
        this.authHandler = authHandler;
    }

    /**
     * Gets all reservations with optional filtering and pagination.
     */
    @GetMapping
    @Operation(summary = "Get all reservations", description = "Retrieves a paginated list of reservations with optional filtering by status or search query")
    public Mono<ResponseEntity<ApiResponse<PageResponse<ReservationDTO>>>> getAllReservations(ServerWebExchange exchange,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by reservation status")
            @RequestParam(required = false) ReservationStatus status,
            @Parameter(description = "Search query for client name or vehicle model/brand")
            @RequestParam(required = false) String query,
            @Parameter(description = "Filter by client CPF")
            @RequestParam(required = false) String cpf) {

        PaginationParams params = validatePagination(page, size);

        return authHandler.getUserInfo(exchange)
            .flatMap(user -> {
                String clientCpf = user.role().equals("CLIENT") ? user.cpf() : cpf;
                return okPage(reservationHandler.getAllPaginated(params.page(), params.size(), status, query, clientCpf));
            })
            .switchIfEmpty(okPage(reservationHandler.getAllPaginated(params.page(), params.size(), status, query, cpf)));
    }

    /**
     * Gets a specific reservation by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves a specific reservation by its ID")
    public Mono<ResponseEntity<ApiResponse<ReservationDTO>>> getReservationById(
            @Parameter(description = "Reservation ID")
            @PathVariable Integer id) {
        return ok(reservationHandler.getById(id));
    }

    /**
     * Creates a new reservation.
     */
    @PostMapping
    @Operation(summary = "Create new reservation", description = "Creates a new vehicle reservation")
    public Mono<ResponseEntity<ApiResponse<ReservationDTO>>> createReservation(
            @RequestBody CreateReservationDTO createReservationDTO) {
        return created(reservationHandler.create(createReservationDTO));
    }

    /**
     * Updates an existing reservation.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update reservation", description = "Updates an existing reservation")
    public Mono<ResponseEntity<ApiResponse<ReservationDTO>>> updateReservation(
            @Parameter(description = "Reservation ID")
            @PathVariable Integer id,
            @RequestBody UpdateReservationDTO updateReservationDTO) {
        return ok(reservationHandler.update(id, updateReservationDTO));
    }

    /**
     * Updates only the status of a reservation.
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status", description = "Updates only the status of a reservation")
    public Mono<ResponseEntity<ApiResponse<ReservationDTO>>> updateReservationStatus(
            @Parameter(description = "Reservation ID")
            @PathVariable Integer id,
            @Parameter(description = "New reservation status")
            @RequestParam ReservationStatus status) {
        return ok(reservationHandler.updateStatus(id, status));
    }

    /**
     * Deletes a reservation.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation", description = "Deletes a reservation (only if status allows)")
    public Mono<ResponseEntity<Void>> deleteReservation(
            @Parameter(description = "Reservation ID")
            @PathVariable Integer id) {
        return reservationHandler.delete(id)
                .then(noContent());
    }

    /**
     * Gets reservations by client CPF.
     */
    @GetMapping("/client/{cpf}")
    @Operation(summary = "Get reservations by client", description = "Retrieves all reservations for a specific client")
    public Mono<ResponseEntity<ApiResponse<PageResponse<ReservationDTO>>>> getReservationsByClient(
            @Parameter(description = "Client CPF")
            @PathVariable String cpf) {
        return okList(reservationHandler.getByClientCpf(cpf));
    }

    /**
     * Gets reservations by vehicle plate.
     */
    @GetMapping("/vehicle/{plate}")
    @Operation(summary = "Get reservations by vehicle", description = "Retrieves all reservations for a specific vehicle")
    public Mono<ResponseEntity<ApiResponse<PageResponse<ReservationDTO>>>> getReservationsByVehicle(
            @Parameter(description = "Vehicle plate")
            @PathVariable String plate) {
        return okList(reservationHandler.getByVehiclePlate(plate));
    }

    /**
     * Gets reservations by status.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get reservations by status", description = "Retrieves all reservations with a specific status")
    public Mono<ResponseEntity<ApiResponse<PageResponse<ReservationDTO>>>> getReservationsByStatus(
            @Parameter(description = "Reservation status") 
            @PathVariable ReservationStatus status) {
        return okList(reservationHandler.getByStatus(status));
    }

    /**
     * Calculates the reservation amount.
     */
    @PostMapping("/calculate-amount")
    @Operation(summary = "Calculate reservation amount", description = "Calculates the total amount for a reservation based on dates, vehicle, and optional promotion code.")
    public Mono<ResponseEntity<ApiResponse<Double>>> calculateReservationAmount(
            @RequestBody CalculateReservationAmountRequestDTO requestDTO) {
        return ok(reservationHandler.calculateReservationAmount(
            requestDTO.startDate(), 
            requestDTO.endDate(), 
            requestDTO.vehiclePlate(), 
            requestDTO.promotionCode()
        ));
    }

    /**
     * Gets reservation report data.
     */
    @GetMapping("/report")
    @Operation(summary = "Get reservation report", description = "Retrieves aggregated data and latest reservations for reporting purposes.")
    public Mono<ResponseEntity<ApiResponse<ReservationReportDTO>>> getReservationReport() {
        return ok(reservationHandler.generateReservationReport());
    }
}