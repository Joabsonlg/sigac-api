package io.github.joabsonlg.sigac_api.maintenance.controller;

import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.maintenance.dto.CreateMaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.dto.MaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.dto.UpdateMaintenanceDTO;
import io.github.joabsonlg.sigac_api.maintenance.handler.MaintenanceHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Controller que expõe endpoints REST para gerenciamento de manutenções.
 * Suporta operações CRUD.
 */
@RestController
@RequestMapping("/api/maintenances")
public class MaintenanceController extends BaseController<MaintenanceDTO, Integer> {

    private final MaintenanceHandler maintenanceHandler;

    public MaintenanceController(MaintenanceHandler maintenanceHandler) {
        this.maintenanceHandler = maintenanceHandler;
    }

    /**
     * Lista todas as manutenções.
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> getAllMaintenances() {
        return maintenanceHandler.getAll()
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }

    /**
     * Busca manutenção pelo ID.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<MaintenanceDTO>>> getMaintenanceById(@PathVariable Long id) {
        return ok(maintenanceHandler.getById(id));
    }

    /**
     * Cria uma nova manutenção.
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<MaintenanceDTO>>> createMaintenance(
            @Valid @RequestBody CreateMaintenanceDTO dto) {
        return created(maintenanceHandler.create(dto));
    }

    /**
     * Atualiza manutenção existente pelo ID.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<MaintenanceDTO>>> updateMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaintenanceDTO dto) {
        return ok(maintenanceHandler.update(id, dto));
    }

    /**
     * Exclui manutenção pelo ID.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteMaintenance(@PathVariable Long id) {
        return maintenanceHandler.delete(id)
                .then(okMessage("Manutenção excluída com sucesso"));
    }
}