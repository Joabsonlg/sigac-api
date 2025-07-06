package io.github.joabsonlg.sigac_api.dailyRate.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.dailyRate.model.DailyRate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.CharSetUtils.count;

/**
 * Repository para operações manuais com a tabela daily_rate.
 */
@Repository
public class DailyRateRepository extends BaseRepository<DailyRate, String> {

    public DailyRateRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    protected String getTableName() {
        return "daily_rate";
    }

    @Override
    protected String getIdColumnName() {
        return "id";
    }

    /**
     * Retorna todas as diárias.
     */
    public Flux<DailyRate> findAll() {
        return databaseClient.sql("""
                SELECT id, amount, date_time, vehicle_plate
                FROM daily_rate
            """)
                .map((row, metadata) -> new DailyRate(
                        row.get("id", Long.class),
                        row.get("amount", Double.class),
                        row.get("date_time", LocalDateTime.class),
                        row.get("vehicle_plate", String.class)
                ))
                .all();
    }

    /**
     * Retorna diária por ID.
     */
    public Mono<DailyRate> findById(Long id) {
        return databaseClient.sql("""
                SELECT id, amount, date_time, vehicle_plate
                FROM daily_rate WHERE id = :id
            """)
                .bind("id", id)
                .map((row, metadata) -> new DailyRate(
                        row.get("id", Long.class),
                        row.get("amount", Double.class),
                        row.get("date_time", LocalDateTime.class),
                        row.get("vehicle_plate", String.class)
                ))
                .one();
    }

    /**
     * Paginação de diárias.
     */
    public Flux<DailyRate> findWithPagination(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT id, amount, date_time, vehicle_plate
                FROM daily_rate
                ORDER BY date_time DESC
                LIMIT :limit OFFSET :offset
            """)
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new DailyRate(
                        row.get("id", Long.class),
                        row.get("amount", Double.class),
                        row.get("date_time", LocalDateTime.class),
                        row.get("vehicle_plate", String.class)
                ))
                .all();
    }

    /**
     * Salva nova diária.
     */
    public Mono<DailyRate> save(DailyRate rate) {
        return databaseClient.sql("""
            INSERT INTO daily_rate (amount, date_time, vehicle_plate)
            VALUES (:amount, :date_time, :vehicle_plate)
            RETURNING id
        """)
                .bind("amount", rate.amount())
                .bind("date_time", rate.dateTime())
                .bind("vehicle_plate", rate.vehiclePlate())
                .map((row, metadata) -> new DailyRate(
                        row.get("id", Long.class),
                        rate.amount(),
                        rate.dateTime(),
                        rate.vehiclePlate()
                ))
                .one();
    }

    /**
     * Atualiza uma diária existente.
     */
    public Mono<DailyRate> update(DailyRate rate) {
        return databaseClient.sql("""
                UPDATE daily_rate
                SET amount = :amount, date_time = :date_time, vehicle_plate = :vehicle_plate
                WHERE id = :id
            """)
                .bind("id", rate.id())
                .bind("amount", rate.amount())
                .bind("date_time", rate.dateTime())
                .bind("vehicle_plate", rate.vehiclePlate())
                .then()
                .thenReturn(rate);
    }

    /**
     * Verifica existência por ID.
     */
    public Mono<Boolean> existsById(String id) {
        return databaseClient.sql("""
                SELECT COUNT(*) FROM daily_rate WHERE id = :id
            """)
                .bind("id", id)
                .map((row, metadata) -> {
                    Long count = row.get(0, Long.class);
                    return count != null && count > 0;
                })
                .one();
    }

    /**
     * Remove diária por ID.
     */
    public Mono<Void> deleteById(String id) {
        return super.deleteById(id);
    }

    /**
     * Conta todas as diárias.
     */
    public Mono<Long> countAll() {
        return count();
    }

    /**
     * Retorna todas as diárias de um veículo.
     */
    public Flux<DailyRate> findByVehiclePlate(String plate) {
        return databaseClient.sql("""
            SELECT id, amount, date_time, vehicle_plate
            FROM daily_rate
            WHERE vehicle_plate = :plate
        """)
                .bind("plate", plate)
                .map((row, metadata) -> new DailyRate(
                        row.get("id", Long.class),
                        row.get("amount", Double.class),
                        row.get("date_time", LocalDateTime.class),
                        row.get("vehicle_plate", String.class)
                ))
                .all();
    }

    /**
     * Retorna a diária mais recente de um veículo.
     */
    public Mono<DailyRate> findMostRecentByVehiclePlate(String plate) {
        return databaseClient.sql("""
            SELECT id, amount, date_time, vehicle_plate
            FROM daily_rate
            WHERE vehicle_plate = :plate
            ORDER BY date_time DESC
            LIMIT 1
        """)
                .bind("plate", plate)
                .map((row, metadata) -> new DailyRate(
                        row.get("id", Long.class),
                        row.get("amount", Double.class),
                        row.get("date_time", LocalDateTime.class),
                        row.get("vehicle_plate", String.class)
                ))
                .one();
    }

}