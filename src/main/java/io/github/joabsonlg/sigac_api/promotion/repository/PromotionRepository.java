package io.github.joabsonlg.sigac_api.promotion.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;
import io.github.joabsonlg.sigac_api.promotion.model.Promotion;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for executing manual SQL queries related to Promotion.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class PromotionRepository extends BaseRepository<Promotion, Integer> {
    
    public PromotionRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "promotion";
    }
    
    @Override
    protected String getIdColumnName() {
        return "code";
    }
    
    /**
     * Public method to count all promotions
     */
    public Mono<Long> countAll() {
        return super.count();
    }
    
    /**
     * Public method to delete promotion by code
     */
    public Mono<Void> deletePromotionByCode(Integer code) {
        return super.deleteById(code);
    }
    
    /**
     * Finds all promotions
     */
    public Flux<Promotion> findAll() {
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            ORDER BY start_date DESC
        """)
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds promotion by code
     */
    public Mono<Promotion> findById(Integer code) {
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE code = :code
        """)
        .bind("code", code)
        .map(this::mapRowToPromotion)
        .one();
    }
    
    /**
     * Finds promotions with pagination
     */
    public Flux<Promotion> findWithPagination(int page, int size) {
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            ORDER BY start_date DESC
        """ + createLimitOffset(page, size))
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds promotions by status with pagination
     */
    public Flux<Promotion> findByStatus(PromotionStatus status, int page, int size) {
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE status = :status
            ORDER BY start_date DESC
        """ + createLimitOffset(page, size))
        .bind("status", mapStatusToString(status))
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds promotions by discount percentage range with pagination
     */
    public Flux<Promotion> findByDiscountRange(Integer minDiscount, Integer maxDiscount, int page, int size) {
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE discount_percentage BETWEEN :min_discount AND :max_discount
            ORDER BY start_date DESC
        """ + createLimitOffset(page, size))
        .bind("min_discount", minDiscount)
        .bind("max_discount", maxDiscount)
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds promotions by status and discount range with pagination
     */
    public Flux<Promotion> findByStatusAndDiscountRange(PromotionStatus status, Integer minDiscount, Integer maxDiscount, int page, int size) {
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE status = :status 
              AND discount_percentage BETWEEN :min_discount AND :max_discount
            ORDER BY start_date DESC
        """ + createLimitOffset(page, size))
        .bind("status", mapStatusToString(status))
        .bind("min_discount", minDiscount)
        .bind("max_discount", maxDiscount)
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds currently valid promotions (active and within date range)
     */
    public Flux<Promotion> findCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE status = 'ATIVA' 
              AND start_date <= :now 
              AND end_date >= :now
            ORDER BY discount_percentage DESC
        """)
        .bind("now", now)
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds promotions that should be automatically activated
     */
    public Flux<Promotion> findScheduledToActivate() {
        LocalDateTime now = LocalDateTime.now();
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE status = 'PROGRAMADA' 
              AND start_date <= :now 
              AND end_date > :now
        """)
        .bind("now", now)
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Finds promotions that should be automatically deactivated
     */
    public Flux<Promotion> findActiveToDeactivate() {
        LocalDateTime now = LocalDateTime.now();
        return databaseClient.sql("""
            SELECT code, discount_percentage, status, start_date, end_date
            FROM promotion
            WHERE status = 'ATIVA' 
              AND end_date < :now
        """)
        .bind("now", now)
        .map(this::mapRowToPromotion)
        .all();
    }
    
    /**
     * Counts promotions by status
     */
    public Mono<Long> countByStatus(PromotionStatus status) {
        return databaseClient.sql("SELECT COUNT(*) FROM promotion WHERE status = :status")
            .bind("status", mapStatusToString(status))
            .map(row -> row.get(0, Long.class))
            .first();
    }
    
    /**
     * Counts promotions by discount range
     */
    public Mono<Long> countByDiscountRange(Integer minDiscount, Integer maxDiscount) {
        return databaseClient.sql("""
            SELECT COUNT(*) FROM promotion 
            WHERE discount_percentage BETWEEN :min_discount AND :max_discount
        """)
        .bind("min_discount", minDiscount)
        .bind("max_discount", maxDiscount)
        .map(row -> row.get(0, Long.class))
        .first();
    }
    
    /**
     * Counts promotions by status and discount range
     */
    public Mono<Long> countByStatusAndDiscountRange(PromotionStatus status, Integer minDiscount, Integer maxDiscount) {
        return databaseClient.sql("""
            SELECT COUNT(*) FROM promotion 
            WHERE status = :status 
              AND discount_percentage BETWEEN :min_discount AND :max_discount
        """)
        .bind("status", mapStatusToString(status))
        .bind("min_discount", minDiscount)
        .bind("max_discount", maxDiscount)
        .map(row -> row.get(0, Long.class))
        .first();
    }
    
    /**
     * Gets count of reservations using a promotion
     */
    public Mono<Long> countReservationsByPromotionCode(Integer promotionCode) {
        return databaseClient.sql("SELECT COUNT(*) FROM reservation WHERE promotion_code = :promotion_code")
            .bind("promotion_code", promotionCode)
            .map(row -> row.get(0, Long.class))
            .first();
    }
    
    /**
     * Saves a new promotion
     */
    public Mono<Promotion> save(Promotion promotion) {
        return databaseClient.sql("""
            INSERT INTO promotion (discount_percentage, status, start_date, end_date)
            VALUES (:discount_percentage, :status, :start_date, :end_date)
            RETURNING code
        """)
        .bind("discount_percentage", promotion.discountPercentage())
        .bind("status", mapStatusToString(promotion.status()))
        .bind("start_date", promotion.startDate())
        .bind("end_date", promotion.endDate())
        .map(row -> row.get("code", Integer.class))
        .one()
        .map(code -> new Promotion(code, promotion.discountPercentage(), promotion.status(),
                                 promotion.startDate(), promotion.endDate()));
    }
    
    /**
     * Updates an existing promotion
     */
    public Mono<Promotion> update(Promotion promotion) {
        return databaseClient.sql("""
            UPDATE promotion 
            SET discount_percentage = :discount_percentage, 
                status = :status,
                start_date = :start_date, 
                end_date = :end_date
            WHERE code = :code
        """)
        .bind("code", promotion.code())
        .bind("discount_percentage", promotion.discountPercentage())
        .bind("status", mapStatusToString(promotion.status()))
        .bind("start_date", promotion.startDate())
        .bind("end_date", promotion.endDate())
        .then()
        .thenReturn(promotion);
    }
    
    /**
     * Updates multiple promotions status (for batch operations)
     */
    public Mono<Void> updateStatusBatch(Flux<Promotion> promotions) {
        return promotions.flatMap(this::update).then();
    }
    
    /**
     * Maps database row to Promotion entity
     */
    private Promotion mapRowToPromotion(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        return new Promotion(
            row.get("code", Integer.class),
            row.get("discount_percentage", Integer.class),
            mapStatusFromString(row.get("status", String.class)),
            row.get("start_date", LocalDateTime.class),
            row.get("end_date", LocalDateTime.class)
        );
    }
    
    /**
     * Maps string status from database to enum
     */
    private PromotionStatus mapStatusFromString(String status) {
        if (status == null) return null;
        return switch (status) {
            case "PROGRAMADA" -> PromotionStatus.SCHEDULED;
            case "ATIVA" -> PromotionStatus.ACTIVE;
            case "INATIVA" -> PromotionStatus.INACTIVE;
            default -> PromotionStatus.valueOf(status); // For new English values
        };
    }
    
    /**
     * Maps enum status to string for database
     */
    private String mapStatusToString(PromotionStatus status) {
        if (status == null) return null;
        return switch (status) {
            case SCHEDULED -> "PROGRAMADA";
            case ACTIVE -> "ATIVA";
            case INACTIVE -> "INATIVA";
        };
    }
}
