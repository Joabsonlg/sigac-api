package io.github.joabsonlg.sigac_api.dashboard.controller;

import io.github.joabsonlg.sigac_api.dashboard.dto.DashboardSummaryDTO;
import io.github.joabsonlg.sigac_api.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller que expõe endpoints REST para gerenciamento de manutenções.
 * Suporta operações CRUD.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public Mono<DashboardSummaryDTO> getResumo() {
        return dashboardService.getResumoDashboard();
    }
}