package io.github.joabsonlg.sigac_api.maintenance.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import io.github.joabsonlg.sigac_api.maintenance.model.Maintenance;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository para operações com manutenção.
 * Extende BaseRepository para operações comuns.
 */
@Repository
public class MaintenanceRepository extends BaseRepository<Maintenance, Long> {

    public MaintenanceRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    protected String getTableName() {
        return "maintenance";
    }

    @Override
    protected String getIdColumnName() {
        return "id";
    }

    /**
     * Busca todas as manutenções.
     */
    public Flux<Maintenance> findAll() {
        return databaseClient.sql("""
                SELECT id, scheduled_date, performed_date, description, type, status, cost, employee_user_cpf, vehicle_plate
                FROM maintenance
            """)
                .map((row, metadata) -> new Maintenance(
                        row.get("id", Long.class),
                        row.get("scheduled_date", java.time.LocalDateTime.class),
                        row.get("performed_date", java.time.LocalDateTime.class),
                        row.get("description", String.class),
                        MaintenanceType.valueOf(row.get("type", String.class)),
                        MaintenanceStatus.valueOf(row.get("status", String.class)),
                        row.get("cost", String.class),
                        row.get("employee_user_cpf", String.class),
                        row.get("vehicle_plate", String.class)
                ))
                .all();
    }

    /**
     * Busca manutenção por id.
     */
    public Mono<Maintenance> findById(Long id) {
        return databaseClient.sql("""
                SELECT id, scheduled_date, performed_date, description, type, status, cost, employee_user_cpf, vehicle_plate
                FROM maintenance WHERE id = :id
            """)
                .bind("id", id)
                .map((row, metadata) -> new Maintenance(
                        row.get("id", Long.class),
                        row.get("scheduled_date", java.time.LocalDateTime.class),
                        row.get("performed_date", java.time.LocalDateTime.class),
                        row.get("description", String.class),
                        MaintenanceType.valueOf(row.get("type", String.class)),
                        MaintenanceStatus.valueOf(row.get("status", String.class)),
                        row.get("cost", String.class),
                        row.get("employee_user_cpf", String.class),
                        row.get("vehicle_plate", String.class)
                ))
                .one();
    }

    /**
     * Salva uma nova manutenção.
     */
    public Mono<Maintenance> save(Maintenance maintenance) {
        var spec = databaseClient.sql("""
        INSERT INTO maintenance (scheduled_date, performed_date, description, type, status, cost, employee_user_cpf, vehicle_plate)
        VALUES (:scheduled_date, :performed_date, :description, :type, :status, :cost, :employee_user_cpf, :vehicle_plate)
    """)
                .bind("scheduled_date", maintenance.scheduledDate());

        spec = maintenance.performedDate() != null
                ? spec.bind("performed_date", maintenance.performedDate())
                : spec.bindNull("performed_date", LocalDateTime.class);

        spec = spec
                .bind("description", maintenance.description())
                .bind("type", maintenance.type().name())
                .bind("status", maintenance.status().name());

        spec = maintenance.cost() != null
                ? spec.bind("cost", maintenance.cost())
                : spec.bindNull("cost", String.class);

        spec = maintenance.employeeUserCpf() != null
                ? spec.bind("employee_user_cpf", maintenance.employeeUserCpf())
                : spec.bindNull("employee_user_cpf", String.class);

        spec = spec.bind("vehicle_plate", maintenance.vehiclePlate());

        return spec.then().thenReturn(maintenance);
    }

    /**
     * Atualiza manutenção existente.
     */
    public Mono<Maintenance> update(Maintenance maintenance) {
        return databaseClient.sql("""
                UPDATE maintenance
                SET scheduled_date = :scheduled_date,
                    performed_date = :performed_date,
                    description = :description,
                    type = :type,
                    status = :status,
                    cost = :cost,
                    employee_user_cpf = :employee_user_cpf,
                    vehicle_plate = :vehicle_plate
                WHERE id = :id
            """)
                .bind("id", maintenance.id())
                .bind("scheduled_date", maintenance.scheduledDate())
                .bind("performed_date", maintenance.performedDate())
                .bind("description", maintenance.description())
                .bind("type", maintenance.type().name())
                .bind("status", maintenance.status().name())
                .bind("cost", maintenance.cost())
                .bind("employee_user_cpf", maintenance.employeeUserCpf())
                .bind("vehicle_plate", maintenance.vehiclePlate())
                .then()
                .thenReturn(maintenance);
    }

    /**
     * Exclui manutenção por id.
     */
    public Mono<Void> deleteById(Long id) {
        return super.deleteById(id);
    }

    /**
     * Verifica se existe manutenção por id.
     */
    public Mono<Boolean> existsById(Long id) {
        return databaseClient.sql("""
                SELECT COUNT(*) FROM maintenance WHERE id = :id
            """)
                .bind("id", id)
                .map((row, metadata) -> {
                    Long count = row.get(0, Long.class);
                    return count != null && count > 0;
                })
                .one();
    }

    /**
     * Conta total de manutenções.
     */
    public Mono<Long> countAll() {
        return count();
    }

    /**
     * Busca manutenções paginadas.
     */
    public Flux<Maintenance> findWithPagination(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT id, scheduled_date, performed_date, description, type, status, cost, employee_user_cpf, vehicle_plate
                FROM maintenance
                ORDER BY scheduled_date DESC
                LIMIT :limit OFFSET :offset
            """)
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new Maintenance(
                        row.get("id", Long.class),
                        row.get("scheduled_date", java.time.LocalDateTime.class),
                        row.get("performed_date", java.time.LocalDateTime.class),
                        row.get("description", String.class),
                        MaintenanceType.valueOf(row.get("type", String.class)),
                        MaintenanceStatus.valueOf(row.get("status", String.class)),
                        row.get("cost", String.class),
                        row.get("employee_user_cpf", String.class),
                        row.get("vehicle_plate", String.class)
                ))
                .all();
    }
}