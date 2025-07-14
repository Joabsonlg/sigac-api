package io.github.joabsonlg.sigac_api.dashboard.dto;

import io.github.joabsonlg.sigac_api.maintenance.model.Maintenance;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardSummaryDTO {

    public static class RecentItem {
        public Long id;
        public String descricao;
        public LocalDate data;
    }

    private long totalVeiculos;
    private long totalClientes;
    private long totalReservas;

    private List<Reservation> reservasRecentes;
    private List<Vehicle> veiculosRecentes;
    private List<Maintenance> manutencoesRecentes;

    // Getters e Setters


    public long getTotalVeiculos() {
        return totalVeiculos;
    }

    public void setTotalVeiculos(long totalVeiculos) {
        this.totalVeiculos = totalVeiculos;
    }

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public long getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(long totalReservas) {
        this.totalReservas = totalReservas;
    }

    public List<Reservation> getReservasRecentes() {
        return reservasRecentes;
    }

    public void setReservasRecentes(List<Reservation> reservasRecentes) {
        this.reservasRecentes = reservasRecentes;
    }

    public List<Vehicle> getVeiculosRecentes() {
        return veiculosRecentes;
    }

    public void setVeiculosRecentes(List<Vehicle> veiculosRecentes) {
        this.veiculosRecentes = veiculosRecentes;
    }

    private BigDecimal receitaBruta;

    public BigDecimal getReceitaBruta() {
        return receitaBruta;
    }

    public void setReceitaBruta(BigDecimal receitaBruta) {
        this.receitaBruta = receitaBruta;
    }
    public List<Maintenance> getManutencoesRecentes() {
        return manutencoesRecentes;
    }

    public void setManutencoesRecentes(List<Maintenance> manutencoesRecentes) {
        this.manutencoesRecentes = manutencoesRecentes;
    }

}