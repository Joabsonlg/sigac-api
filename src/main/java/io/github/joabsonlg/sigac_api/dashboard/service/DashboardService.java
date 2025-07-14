package io.github.joabsonlg.sigac_api.dashboard.service;

import io.github.joabsonlg.sigac_api.dashboard.dto.DashboardSummaryDTO;
import io.github.joabsonlg.sigac_api.dashboard.repository.DashboardRepository;
import io.github.joabsonlg.sigac_api.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import io.github.joabsonlg.sigac_api.maintenance.model.Maintenance;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(DashboardRepository dashboardRepository, PaymentRepository paymentRepository) {
        this.dashboardRepository = dashboardRepository;
        this.paymentRepository = paymentRepository;
    }

    public Mono<DashboardSummaryDTO> getResumoDashboard() {
        Mono<Long> totalVeiculos = dashboardRepository.countTotalVeiculos();
        Mono<Long> totalClientes = dashboardRepository.countTotalClientes();
        Mono<Long> totalReservas = dashboardRepository.countTotalReservas();
        Mono<List<Reservation>> reservasRecentes = dashboardRepository.findTop5ReservasRecentes().collectList();
        Mono<List<Vehicle>> veiculosRecentes = dashboardRepository.findTop5VeiculosRecentes().collectList();
        Mono<List<Maintenance>> manutencoesRecentes = dashboardRepository.findTop5ManutencoesRecentes().collectList();
        Mono<BigDecimal> receitaBruta = paymentRepository.calcularReceitaBruta();

        return Mono.zip(
                totalVeiculos,
                totalClientes,
                totalReservas,
                reservasRecentes,
                veiculosRecentes,
                manutencoesRecentes,
                receitaBruta
        ).map(tuple -> {
            DashboardSummaryDTO dto = new DashboardSummaryDTO();
            dto.setTotalVeiculos(tuple.getT1());
            dto.setTotalClientes(tuple.getT2());
            dto.setTotalReservas(tuple.getT3());
            dto.setReservasRecentes(tuple.getT4());
            dto.setVeiculosRecentes(tuple.getT5());
            dto.setManutencoesRecentes(tuple.getT6());
            dto.setReceitaBruta(tuple.getT7());
            return dto;
        });
    }
}