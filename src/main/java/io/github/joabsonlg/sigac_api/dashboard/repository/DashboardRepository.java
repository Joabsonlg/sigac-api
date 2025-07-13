package io.github.joabsonlg.sigac_api.dashboard.repository;

import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import io.github.joabsonlg.sigac_api.maintenance.model.Maintenance;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public class DashboardRepository {

    private final DatabaseClient databaseClient;

    public DashboardRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    // --- Contagem total de veículos ---
    public Mono<Long> countTotalVeiculos() {
        return databaseClient.sql("SELECT COUNT(*) FROM vehicle")
                .map(row -> row.get(0, Long.class))
                .one();
    }

    // --- Contagem total de clientes ---
    public Mono<Long> countTotalClientes() {
        return databaseClient.sql("SELECT COUNT(*) FROM client")
                .map(row -> row.get(0, Long.class))
                .one();
    }

    // --- Contagem total de reservas ---
    public Mono<Long> countTotalReservas() {
        return databaseClient.sql("SELECT COUNT(*) FROM reservation")
                .map(row -> row.get(0, Long.class))
                .one();
    }

    // --- Receita mensal somada (valor de reservas iniciadas neste mês) ---
    public Mono<BigDecimal> calcularReceitaMensal() {
        return databaseClient.sql("""
            SELECT COALESCE(SUM(value), 0)
            FROM reservation
            WHERE EXTRACT(MONTH FROM start_date) = EXTRACT(MONTH FROM CURRENT_DATE)
              AND EXTRACT(YEAR FROM start_date) = EXTRACT(YEAR FROM CURRENT_DATE)
            """)
                .map(row -> row.get(0, BigDecimal.class))
                .one();
    }

    // --- Faturamento mensal (reservas concluídas no mês) ---
    public Mono<BigDecimal> calcularFaturamentoMensal() {
        return databaseClient.sql("""
        SELECT COALESCE(SUM(value), 0)
        FROM reservation
        WHERE status = 'COMPLETED'
          AND EXTRACT(MONTH FROM end_date) = EXTRACT(MONTH FROM CURRENT_DATE)
          AND EXTRACT(YEAR FROM end_date) = EXTRACT(YEAR FROM CURRENT_DATE)
        """)
                .map(row -> row.get(0, BigDecimal.class))
                .one();
    }

    // --- Últimas 5 reservas completas (ordenadas por data da reserva) ---
    public Flux<Reservation> findTop5ReservasRecentes() {
        return databaseClient.sql("""
            SELECT id, start_date, end_date, reservation_date, status,
                   promotion_code, client_user_cpf, employee_user_cpf, vehicle_plate
            FROM reservation
            ORDER BY reservation_date DESC
            LIMIT 5
            """)
                .map(this::mapRowToReservation)
                .all();
    }

    // --- Últimos 5 veículos completos (ordenados por data de criação) ---
    public Flux<Vehicle> findTop5VeiculosRecentes() {
        return databaseClient.sql("""
        SELECT plate, year, model, brand, status, image_url
        FROM vehicle
        ORDER BY plate DESC
        LIMIT 5
        """)
                .map(this::mapRowToVehicle)
                .all();
    }

    // --- Últimas 5 manutenções completas (ordenadas por data agendada) ---
    public Flux<Maintenance> findTop5ManutencoesRecentes() {
        return databaseClient.sql("""
            SELECT id, scheduled_date, performed_date, description, type, status, cost, employee_user_cpf, vehicle_plate
            FROM maintenance
            ORDER BY scheduled_date DESC
            LIMIT 3
            """)
                .map(this::mapRowToMaintenance)
                .all();
    }

    // ======= Mapeamentos =======

    private Reservation mapRowToReservation(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        return new Reservation(
                row.get("id", Integer.class),
                row.get("start_date", LocalDateTime.class),
                row.get("end_date", LocalDateTime.class),
                row.get("reservation_date", LocalDateTime.class),
                mapStatusFromString(row.get("status", String.class)),
                row.get("promotion_code", Integer.class),
                row.get("client_user_cpf", String.class),
                row.get("employee_user_cpf", String.class),
                row.get("vehicle_plate", String.class)
        );
    }

    private Vehicle mapRowToVehicle(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        return new Vehicle(
                row.get("plate", String.class),
                row.get("year", Integer.class),
                row.get("model", String.class),
                row.get("brand", String.class),
                VehicleStatus.valueOf(row.get("status", String.class)),
                row.get("image_url", String.class)
        );
    }

    private Maintenance mapRowToMaintenance(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        return new Maintenance(
                row.get("id", Long.class),
                row.get("scheduled_date", LocalDateTime.class),
                row.get("performed_date", LocalDateTime.class),
                row.get("description", String.class),
                MaintenanceType.valueOf(row.get("type", String.class)),
                MaintenanceStatus.valueOf(row.get("status", String.class)),
                row.get("cost", String.class),
                row.get("employee_user_cpf", String.class),
                row.get("vehicle_plate", String.class)
        );
    }

    private ReservationStatus mapStatusFromString(String status) {
        if (status == null) return null;
        try {
            return ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}