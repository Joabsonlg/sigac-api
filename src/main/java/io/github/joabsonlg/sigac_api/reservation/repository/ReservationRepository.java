package io.github.joabsonlg.sigac_api.reservation.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import io.github.joabsonlg.sigac_api.reservation.model.Reservation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for executing manual SQL queries related to Reservation.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class ReservationRepository extends BaseRepository<Reservation, Integer> {
    
    public ReservationRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "reservation";
    }
    
    /**
     * Finds all reservations
     */
    public Flux<Reservation> findAll() {
        return databaseClient.sql("""
            SELECT id, start_date, end_date, reservation_date, status, 
                   promotion_code, client_user_cpf, employee_user_cpf, vehicle_plate
            FROM reservation
            ORDER BY reservation_date DESC
        """)
        .map(this::mapRowToReservation)
        .all();
    }
    
    /**
     * Finds reservation by ID
     */
    public Mono<Reservation> findById(Integer id) {
        return databaseClient.sql("""
            SELECT id, start_date, end_date, reservation_date, status, 
                   promotion_code, client_user_cpf, employee_user_cpf, vehicle_plate
            FROM reservation
            WHERE id = :id
        """)
        .bind("id", id)
        .map(this::mapRowToReservation)
        .one();
    }
    
    /**
     * Finds reservations with complete information including client and vehicle details
     */
    public Flux<Object[]> findAllWithDetails() {
        return databaseClient.sql("""
            SELECT r.id, r.start_date, r.end_date, r.reservation_date, r.status, 
                   r.promotion_code, r.client_user_cpf, u_client.name as client_name,
                   r.employee_user_cpf, u_employee.name as employee_name,
                   r.vehicle_plate, v.model as vehicle_model, v.brand as vehicle_brand
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN employee e ON r.employee_user_cpf = e.user_cpf
            LEFT JOIN users u_employee ON e.user_cpf = u_employee.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            ORDER BY r.reservation_date DESC
        """)
        .map((row, metadata) -> new Object[]{
            row.get("id", Integer.class),
            row.get("start_date", LocalDateTime.class),
            row.get("end_date", LocalDateTime.class),
            row.get("reservation_date", LocalDateTime.class),
            mapStatusFromString(row.get("status", String.class)),
            row.get("promotion_code", Integer.class),
            row.get("client_user_cpf", String.class),
            row.get("client_name", String.class),
            row.get("employee_user_cpf", String.class),
            row.get("employee_name", String.class),
            row.get("vehicle_plate", String.class),
            row.get("vehicle_model", String.class),
            row.get("vehicle_brand", String.class)
        })
        .all();
    }
    
    /**
     * Finds reservations with pagination
     */
    public Flux<Reservation> findWithPagination(int page, int size) {
        return databaseClient.sql("""
            SELECT id, start_date, end_date, reservation_date, status, 
                   promotion_code, client_user_cpf, employee_user_cpf, vehicle_plate
            FROM reservation
            ORDER BY reservation_date DESC
        """ + createLimitOffset(page, size))
        .map(this::mapRowToReservation)
        .all();
    }
    
    /**
     * Finds reservations with complete information and pagination
     */
    public Flux<Object[]> findWithDetailsAndPagination(int page, int size) {
        return databaseClient.sql("""
            SELECT r.id, r.start_date, r.end_date, r.reservation_date, r.status, 
                   r.promotion_code, r.client_user_cpf, u_client.name as client_name,
                   r.employee_user_cpf, u_employee.name as employee_name,
                   r.vehicle_plate, v.model as vehicle_model, v.brand as vehicle_brand
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN employee e ON r.employee_user_cpf = e.user_cpf
            LEFT JOIN users u_employee ON e.user_cpf = u_employee.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            ORDER BY r.reservation_date DESC
        """ + createLimitOffset(page, size))
        .map((row, metadata) -> new Object[]{
            row.get("id", Integer.class),
            row.get("start_date", LocalDateTime.class),
            row.get("end_date", LocalDateTime.class),
            row.get("reservation_date", LocalDateTime.class),
            mapStatusFromString(row.get("status", String.class)),
            row.get("promotion_code", Integer.class),
            row.get("client_user_cpf", String.class),
            row.get("client_name", String.class),
            row.get("employee_user_cpf", String.class),
            row.get("employee_name", String.class),
            row.get("vehicle_plate", String.class),
            row.get("vehicle_model", String.class),
            row.get("vehicle_brand", String.class)
        })
        .all();
    }
    
    /**
     * Finds reservations by status with pagination
     */
    public Flux<Object[]> findByStatusWithDetails(ReservationStatus status, int page, int size) {
        return databaseClient.sql("""
            SELECT r.id, r.start_date, r.end_date, r.reservation_date, r.status, 
                   r.promotion_code, r.client_user_cpf, u_client.name as client_name,
                   r.employee_user_cpf, u_employee.name as employee_name,
                   r.vehicle_plate, v.model as vehicle_model, v.brand as vehicle_brand
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN employee e ON r.employee_user_cpf = e.user_cpf
            LEFT JOIN users u_employee ON e.user_cpf = u_employee.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            WHERE r.status = :status
            ORDER BY r.reservation_date DESC
        """ + createLimitOffset(page, size))
        .bind("status", mapStatusToString(status))
        .map((row, metadata) -> new Object[]{
            row.get("id", Integer.class),
            row.get("start_date", LocalDateTime.class),
            row.get("end_date", LocalDateTime.class),
            row.get("reservation_date", LocalDateTime.class),
            mapStatusFromString(row.get("status", String.class)),
            row.get("promotion_code", Integer.class),
            row.get("client_user_cpf", String.class),
            row.get("client_name", String.class),
            row.get("employee_user_cpf", String.class),
            row.get("employee_name", String.class),
            row.get("vehicle_plate", String.class),
            row.get("vehicle_model", String.class),
            row.get("vehicle_brand", String.class)
        })
        .all();
    }
    
    /**
     * Finds reservations by query (client name or vehicle model/brand) with pagination
     */
    public Flux<Object[]> findByQueryWithDetails(String query, int page, int size) {
        return databaseClient.sql("""
            SELECT r.id, r.start_date, r.end_date, r.reservation_date, r.status, 
                   r.promotion_code, r.client_user_cpf, u_client.name as client_name,
                   r.employee_user_cpf, u_employee.name as employee_name,
                   r.vehicle_plate, v.model as vehicle_model, v.brand as vehicle_brand
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN employee e ON r.employee_user_cpf = e.user_cpf
            LEFT JOIN users u_employee ON e.user_cpf = u_employee.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            WHERE LOWER(u_client.name) LIKE LOWER(:query) 
               OR LOWER(v.model) LIKE LOWER(:query)
               OR LOWER(v.brand) LIKE LOWER(:query)
               OR r.vehicle_plate LIKE UPPER(:query)
            ORDER BY r.reservation_date DESC
        """ + createLimitOffset(page, size))
        .bind("query", "%" + query + "%")
        .map((row, metadata) -> new Object[]{
            row.get("id", Integer.class),
            row.get("start_date", LocalDateTime.class),
            row.get("end_date", LocalDateTime.class),
            row.get("reservation_date", LocalDateTime.class),
            mapStatusFromString(row.get("status", String.class)),
            row.get("promotion_code", Integer.class),
            row.get("client_user_cpf", String.class),
            row.get("client_name", String.class),
            row.get("employee_user_cpf", String.class),
            row.get("employee_name", String.class),
            row.get("vehicle_plate", String.class),
            row.get("vehicle_model", String.class),
            row.get("vehicle_brand", String.class)
        })
        .all();
    }
    
    /**
     * Finds reservations by status and query with pagination
     */
    public Flux<Object[]> findByStatusAndQueryWithDetails(ReservationStatus status, String query, int page, int size) {
        return databaseClient.sql("""
            SELECT r.id, r.start_date, r.end_date, r.reservation_date, r.status, 
                   r.promotion_code, r.client_user_cpf, u_client.name as client_name,
                   r.employee_user_cpf, u_employee.name as employee_name,
                   r.vehicle_plate, v.model as vehicle_model, v.brand as vehicle_brand
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN employee e ON r.employee_user_cpf = e.user_cpf
            LEFT JOIN users u_employee ON e.user_cpf = u_employee.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            WHERE r.status = :status
              AND (LOWER(u_client.name) LIKE LOWER(:query) 
                   OR LOWER(v.model) LIKE LOWER(:query)
                   OR LOWER(v.brand) LIKE LOWER(:query)
                   OR r.vehicle_plate LIKE UPPER(:query))
            ORDER BY r.reservation_date DESC
        """ + createLimitOffset(page, size))
        .bind("status", mapStatusToString(status))
        .bind("query", "%" + query + "%")
        .map((row, metadata) -> new Object[]{
            row.get("id", Integer.class),
            row.get("start_date", LocalDateTime.class),
            row.get("end_date", LocalDateTime.class),
            row.get("reservation_date", LocalDateTime.class),
            mapStatusFromString(row.get("status", String.class)),
            row.get("promotion_code", Integer.class),
            row.get("client_user_cpf", String.class),
            row.get("client_name", String.class),
            row.get("employee_user_cpf", String.class),
            row.get("employee_name", String.class),
            row.get("vehicle_plate", String.class),
            row.get("vehicle_model", String.class),
            row.get("vehicle_brand", String.class)
        })
        .all();
    }
    
    /**
     * Counts reservations by status
     */
    public Mono<Long> countByStatus(ReservationStatus status) {
        return databaseClient.sql("SELECT COUNT(*) FROM reservation WHERE status = :status")
            .bind("status", mapStatusToString(status))
            .map(row -> row.get(0, Long.class))
            .first();
    }
    
    /**
     * Counts reservations by query
     */
    public Mono<Long> countByQuery(String query) {
        return databaseClient.sql("""
            SELECT COUNT(*) 
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            WHERE LOWER(u_client.name) LIKE LOWER(:query) 
               OR LOWER(v.model) LIKE LOWER(:query)
               OR LOWER(v.brand) LIKE LOWER(:query)
               OR r.vehicle_plate LIKE UPPER(:query)
        """)
        .bind("query", "%" + query + "%")
        .map(row -> row.get(0, Long.class))
        .first();
    }
    
    /**
     * Counts reservations by status and query
     */
    public Mono<Long> countByStatusAndQuery(ReservationStatus status, String query) {
        return databaseClient.sql("""
            SELECT COUNT(*) 
            FROM reservation r
            LEFT JOIN client c ON r.client_user_cpf = c.user_cpf
            LEFT JOIN users u_client ON c.user_cpf = u_client.cpf
            LEFT JOIN vehicle v ON r.vehicle_plate = v.plate
            WHERE r.status = :status
              AND (LOWER(u_client.name) LIKE LOWER(:query) 
                   OR LOWER(v.model) LIKE LOWER(:query)
                   OR LOWER(v.brand) LIKE LOWER(:query)
                   OR r.vehicle_plate LIKE UPPER(:query))
        """)
        .bind("status", mapStatusToString(status))
        .bind("query", "%" + query + "%")
        .map(row -> row.get(0, Long.class))
        .first();
    }
    
    /**
     * Saves a new reservation
     */
    public Mono<Reservation> save(Reservation reservation) {
        return databaseClient.sql("""
            INSERT INTO reservation (start_date, end_date, reservation_date, status, 
                                   promotion_code, client_user_cpf, employee_user_cpf, vehicle_plate)
            VALUES (:start_date, :end_date, :reservation_date, :status, 
                    :promotion_code, :client_user_cpf, :employee_user_cpf, :vehicle_plate)
            RETURNING id
        """)
        .bind("start_date", reservation.startDate())
        .bind("end_date", reservation.endDate())
        .bind("reservation_date", reservation.reservationDate())
        .bind("status", mapStatusToString(reservation.status()))
        .bind("promotion_code", reservation.promotionCode())
        .bind("client_user_cpf", reservation.clientUserCpf())
        .bind("employee_user_cpf", reservation.employeeUserCpf())
        .bind("vehicle_plate", reservation.vehiclePlate())
        .map(row -> row.get("id", Integer.class))
        .one()
        .map(id -> new Reservation(id, reservation.startDate(), reservation.endDate(),
                                 reservation.reservationDate(), reservation.status(),
                                 reservation.promotionCode(), reservation.clientUserCpf(),
                                 reservation.employeeUserCpf(), reservation.vehiclePlate()));
    }
    
    /**
     * Updates an existing reservation
     */
    public Mono<Reservation> update(Reservation reservation) {
        return databaseClient.sql("""
            UPDATE reservation 
            SET start_date = :start_date, end_date = :end_date, status = :status,
                promotion_code = :promotion_code, employee_user_cpf = :employee_user_cpf,
                vehicle_plate = :vehicle_plate
            WHERE id = :id
        """)
        .bind("id", reservation.id())
        .bind("start_date", reservation.startDate())
        .bind("end_date", reservation.endDate())
        .bind("status", mapStatusToString(reservation.status()))
        .bind("promotion_code", reservation.promotionCode())
        .bind("employee_user_cpf", reservation.employeeUserCpf())
        .bind("vehicle_plate", reservation.vehiclePlate())
        .then()
        .thenReturn(reservation);
    }
    
    /**
     * Checks if a vehicle is available for the given date range
     */
    public Mono<Boolean> isVehicleAvailable(String vehiclePlate, LocalDateTime startDate, LocalDateTime endDate, Integer excludeReservationId) {
        String sql = """
            SELECT COUNT(*) 
            FROM reservation 
            WHERE vehicle_plate = :vehicle_plate 
              AND status IN ('CONFIRMED', 'IN_PROGRESS')
              AND (
                (:start_date BETWEEN start_date AND end_date) OR
                (:end_date BETWEEN start_date AND end_date) OR
                (start_date BETWEEN :start_date AND :end_date) OR
                (end_date BETWEEN :start_date AND :end_date)
              )
        """;
        
        if (excludeReservationId != null) {
            sql += " AND id != :exclude_id";
        }
        
        var query = databaseClient.sql(sql)
            .bind("vehicle_plate", vehiclePlate)
            .bind("start_date", startDate)
            .bind("end_date", endDate);
            
        if (excludeReservationId != null) {
            query = query.bind("exclude_id", excludeReservationId);
        }
        
        return query.map(row -> row.get(0, Long.class))
            .first()
            .map(count -> count == 0);
    }
    
    /**
     * Maps database row to Reservation entity
     */
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
    
    /**
     * Maps string status from database to enum
     */
    private ReservationStatus mapStatusFromString(String status) {
        if (status == null) return null;
        return switch (status) {
            case "PENDENTE" -> ReservationStatus.PENDING;
            case "CONFIRMADA" -> ReservationStatus.CONFIRMED;
            case "EM_ANDAMENTO" -> ReservationStatus.IN_PROGRESS;
            case "FINALIZADA" -> ReservationStatus.COMPLETED;
            case "CANCELADA" -> ReservationStatus.CANCELLED;
            default -> ReservationStatus.valueOf(status); // For new English values
        };
    }
    
    /**
     * Maps enum status to string for database
     */
    private String mapStatusToString(ReservationStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PENDING -> "PENDENTE";
            case CONFIRMED -> "CONFIRMADA";
            case IN_PROGRESS -> "EM_ANDAMENTO";
            case COMPLETED -> "FINALIZADA";
            case CANCELLED -> "CANCELADA";
        };
    }
    
    /**
     * Public method to count all reservations
     */
    public Mono<Long> countAll() {
        return super.count();
    }
    
    /**
     * Public method to delete reservation by ID
     */
    public Mono<Void> deleteReservationById(Integer id) {
        return super.deleteById(id);
    }
}
