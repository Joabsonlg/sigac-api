package io.github.joabsonlg.sigac_api.dashboard.service;

import io.github.joabsonlg.sigac_api.dashboard.dto.DashboardSummaryDTO;
import io.github.joabsonlg.sigac_api.dashboard.repository.DashboardRepository;
import io.github.joabsonlg.sigac_api.maintenance.model.Maintenance;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public Mono<DashboardSummaryDTO> getResumoDashboard() {
        Mono<Long> totalVeiculos = dashboardRepository.countTotalVeiculos();
        Mono<Long> totalClientes = dashboardRepository.countTotalClientes();
        Mono<Long> reservasAtivas = dashboardRepository.countReservasAtivas();
        Mono<List<Reservation>> reservasRecentes = dashboardRepository.findTop5ReservasRecentes().collectList();
        Mono<List<Vehicle>> veiculosRecentes = dashboardRepository.findTop5VeiculosRecentes().collectList();
        Mono<List<Maintenance>> manutencoesRecentes = dashboardRepository.findTop5ManutencoesRecentes().collectList();

        return Mono.zip(
                totalVeiculos,
                totalClientes,
                reservasAtivas,
                reservasRecentes,
                veiculosRecentes,
                manutencoesRecentes
        ).map(tuple -> {
            DashboardSummaryDTO dto = new DashboardSummaryDTO();
            dto.setTotalVeiculos(tuple.getT1());
            dto.setTotalClientes(tuple.getT2());
            dto.setReservasAtivas(tuple.getT3());
            dto.setReservasRecentes(tuple.getT4());
            dto.setVeiculosRecentes(tuple.getT5());
            dto.setManutencoesRecentes(tuple.getT6());
            return dto;
        });
    }
}