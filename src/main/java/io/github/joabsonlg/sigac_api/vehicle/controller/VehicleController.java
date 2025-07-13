package io.github.joabsonlg.sigac_api.vehicle.controller;

import io.github.joabsonlg.sigac_api.auth.handler.AuthHandler;
import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.vehicle.dto.CreateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.UpdateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.VehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.VehicleReportDTO;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import io.github.joabsonlg.sigac_api.vehicle.handler.VehicleHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * Controller que expõe endpoints REST para gerenciamento de veículos.
 * Suporta operações CRUD, paginação e buscas específicas.
 * Segue padrão semelhante ao UserController para consistência na API.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController extends BaseController<VehicleDTO, String> {

    private final VehicleHandler vehicleHandler;
    private final AuthHandler authHandler;

    public VehicleController(VehicleHandler vehicleHandler, AuthHandler authHandler) {
        this.vehicleHandler = vehicleHandler;
        this.authHandler = authHandler;
    }

    /**
     * Lista todos os veículos com paginação opcional
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            ServerWebExchange exchange) {

        return authHandler.getUserInfo(exchange)
                .flatMap(user -> {
                    boolean isClient = user.role().equals("CLIENT");
                    VehicleStatus status = isClient ? VehicleStatus.DISPONIVEL : null;
                    if (page >= 0 && size > 0) {
                        return vehicleHandler.getAllPaginated(page, size, status)
                                .map(pageResponse -> ResponseEntity.ok(ApiResponse.success((Object) pageResponse)));
                    } else {
                        return vehicleHandler.getAll(status)
                                .collectList()
                                .map(vehicles -> ResponseEntity.ok(ApiResponse.success((Object) vehicles)));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    if (page >= 0 && size > 0) {
                        return vehicleHandler.getAllPaginated(page, size, null)
                                .map(pageResponse -> ResponseEntity.ok(ApiResponse.success((Object) pageResponse)));
                    } else {
                        return vehicleHandler.getAll(null)
                                .collectList()
                                .map(vehicles -> ResponseEntity.ok(ApiResponse.success((Object) vehicles)));
                    }
                }));
    }

    /**
     * Busca veículo pelo número da placa
     */
    @GetMapping("/{plate}")
    public Mono<ResponseEntity<ApiResponse<VehicleDTO>>> getVehicleByPlate(@PathVariable String plate) {
        return ok(vehicleHandler.getById(plate));
    }

    /**
     * Cria um novo veículo
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<VehicleDTO>>> createVehicle(@Valid @RequestBody CreateVehicleDTO dto) {
        return created(vehicleHandler.create(dto));
    }

    /**
     * Atualiza um veículo existente identificado pela placa
     */
    @PutMapping("/{plate}")
    public Mono<ResponseEntity<ApiResponse<VehicleDTO>>> updateVehicle(
            @PathVariable String plate,
            @Valid @RequestBody UpdateVehicleDTO dto) {
        return ok(vehicleHandler.update(plate, dto));
    }

    /**
     * Exclui um veículo pela placa
     */
    @DeleteMapping("/{plate}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteVehicle(@PathVariable String plate) {
        return vehicleHandler.delete(plate)
                .then(okMessage("Veículo excluído com sucesso"));
    }

    /**
     * Verifica se veículo existe pela placa
     */
    @GetMapping("/{plate}/exists")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> vehicleExists(@PathVariable String plate) {
        return ok(vehicleHandler.existsByPlate(plate));
    }

    /**
     * Gets vehicle report data.
     */
    @GetMapping("/report")
    public Mono<ResponseEntity<ApiResponse<VehicleReportDTO>>> getVehicleReport() {
        return ok(vehicleHandler.generateVehicleReport());
    }
}