package io.github.joabsonlg.sigac_api.vehicle.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import io.github.joabsonlg.sigac_api.vehicle.model.Vehicle;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for executing manual SQL queries related to Vehicle.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class VehicleRepository extends BaseRepository<Vehicle, String> {

    public VehicleRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    protected String getTableName() {
        return "vehicle";
    }

    @Override
    protected String getIdColumnName() {
        return "plate";
    }

    /**
     * Finds all vehicles.
     */
    public Flux<Vehicle> findAll() {
        return databaseClient.sql("""
                SELECT plate, year, model, brand, status, image_url
                FROM vehicle
            """)
                .map((row, metadata) -> new Vehicle(
                        row.get("plate", String.class),
                        row.get("year", Integer.class),
                        row.get("model", String.class),
                        row.get("brand", String.class),
                        VehicleStatus.valueOf(row.get("status", String.class)),
                        row.get("image_url", String.class)
                ))
                .all();
    }

    /**
     * Finds vehicle by plate.
     */
    public Mono<Vehicle> findById(String plate) {
        return databaseClient.sql("""
                SELECT plate, year, model, brand, status, image_url
                FROM vehicle WHERE plate = :plate
            """)
                .bind("plate", plate)
                .map((row, metadata) -> new Vehicle(
                        row.get("plate", String.class),
                        row.get("year", Integer.class),
                        row.get("model", String.class),
                        row.get("brand", String.class),
                        VehicleStatus.valueOf(row.get("status", String.class)),
                        row.get("image_url", String.class)
                ))
                .one();
    }

    /**
     * Finds vehicles with pagination.
     */
    public Flux<Vehicle> findWithPagination(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT plate, year, model, brand, status, image_url
                FROM vehicle
                ORDER BY model
                LIMIT :limit OFFSET :offset
            """)
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new Vehicle(
                        row.get("plate", String.class),
                        row.get("year", Integer.class),
                        row.get("model", String.class),
                        row.get("brand", String.class),
                        VehicleStatus.valueOf(row.get("status", String.class)),
                        row.get("image_url", String.class)
                ))
                .all();
    }

    /**
     * Saves a new vehicle.
     */
    public Mono<Vehicle> save(Vehicle vehicle) {
        if (vehicle.plate() == null) {
            throw new IllegalArgumentException("Placa é obrigatória para criar um veículo");
        }

        return databaseClient.sql("""
                INSERT INTO vehicle (plate, year, model, brand, status, image_url)
                VALUES (:plate, :year, :model, :brand, :status, :image_url)
            """)
                .bind("plate", vehicle.plate())
                .bind("year", vehicle.year())
                .bind("model", vehicle.model())
                .bind("brand", vehicle.brand())
                .bind("status", vehicle.status().name())
                .bind("image_url", vehicle.imageUrl())
                .then()
                .thenReturn(vehicle);
    }

    /**
     * Updates an existing vehicle.
     */
    public Mono<Vehicle> update(Vehicle vehicle) {
        return databaseClient.sql("""
                UPDATE vehicle
                SET year = :year, model = :model, brand = :brand, status = :status, image_url = :image_url
                WHERE plate = :plate
            """)
                .bind("plate", vehicle.plate())
                .bind("year", vehicle.year())
                .bind("model", vehicle.model())
                .bind("brand", vehicle.brand())
                .bind("status", vehicle.status().name())
                .bind("image_url", vehicle.imageUrl())
                .then()
                .thenReturn(vehicle);
    }

    /**
     * Checks if a vehicle exists by plate.
     */
    public Mono<Boolean> existsByPlate(String plate) {
        return databaseClient.sql("""
            SELECT COUNT(*) FROM vehicle WHERE plate = :plate
        """)
                .bind("plate", plate)
                .map((row, metadata) -> {
                    Long count = row.get(0, Long.class);
                    return count != null && count > 0;
                })
                .one()
                .defaultIfEmpty(false);
    }

    /**
     * Deletes a vehicle by plate.
     */
    public Mono<Void> deleteByPlate(String plate) {
        return deleteById(plate);
    }

    /**
     * Gets total count of vehicles.
     */
    public Mono<Long> countAll() {
        return count();
    }
}
