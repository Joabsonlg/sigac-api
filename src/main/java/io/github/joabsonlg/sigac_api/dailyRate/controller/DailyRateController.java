package io.github.joabsonlg.sigac_api.dailyRate.controller;

import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.dailyRate.dto.DailyRateDTO;
import io.github.joabsonlg.sigac_api.dailyRate.dto.DailyRateInputDTO;
import io.github.joabsonlg.sigac_api.dailyRate.handler.DailyRateHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller REST para gerenciamento de DailyRates.
 * Segue padrão semelhante ao VehicleController.
 */
@RestController
@RequestMapping("/api/dailyrates")
public class DailyRateController extends BaseController<DailyRateDTO, Integer> {

    private final DailyRateHandler dailyRateHandler;

    public DailyRateController(DailyRateHandler dailyRateHandler) {
        this.dailyRateHandler = dailyRateHandler;
    }

    /**
     * Lista todas as DailyRates com paginação opcional
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> getAllDailyRates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page >= 0 && size > 0) {
            return dailyRateHandler.getAllPaginated(page, size)
                    .map(pageResponse -> ResponseEntity.ok(ApiResponse.success(pageResponse)));
        } else {
            return dailyRateHandler.getAll()
                    .collectList()
                    .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
        }
    }

    /**
     * Busca DailyRate por id
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<DailyRateDTO>>> getDailyRateById(@PathVariable Long id) {
        return ok(dailyRateHandler.getById(id));
    }

    /**
     * Cria um novo DailyRate
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<DailyRateDTO>>> createDailyRate(@Valid @RequestBody DailyRateInputDTO dto) {
        return created(dailyRateHandler.create(dto));
    }

    /**
     * Exclui DailyRate por id
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteDailyRate(@PathVariable Long id) {
        return dailyRateHandler.delete(id)
                .then(okMessage("DailyRate excluído com sucesso"));
    }

    /**
     * Retorna todas as DailyRates associadas a um veículo.
     *
     * @param plate placa do veículo
     * @return ResponseEntity com a lista de DailyRates do veículo
     */
    @GetMapping("/vehicle/{plate}")
    public Mono<ResponseEntity<ApiResponse<Object>>> getDailyRatesByVehicle(@PathVariable String plate) {
        return dailyRateHandler.getByVehiclePlate(plate)
                .collectList()
                .map(rates -> ResponseEntity.ok(ApiResponse.success(rates)));
    }

    /**
     * Retorna a DailyRate mais recente (atual) de um veículo.
     *
     * @param plate placa do veículo
     * @return ResponseEntity com a última DailyRate cadastrada
     */
    @GetMapping("/vehicle/{plate}/current")
    public Mono<ResponseEntity<ApiResponse<DailyRateDTO>>> getCurrentDailyRateByVehicle(@PathVariable String plate) {
        return ok(dailyRateHandler.getMostRecentByVehiclePlate(plate));
    }
}