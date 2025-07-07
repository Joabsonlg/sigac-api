package io.github.joabsonlg.sigac_api.promotion.model;

import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity model representing a Promotion in the system.
 * Manages discount promotions that can be applied to reservations.
 */
@Table("promotion")
public record Promotion(
        @Id
        @Column("code")
        Integer code,

        @Column("discount_percentage")
        Integer discountPercentage,

        @Column("status")
        PromotionStatus status,

        @Column("start_date")
        LocalDateTime startDate,

        @Column("end_date")
        LocalDateTime endDate
) {
    /**
     * Constructor for creating a new promotion (without code for auto-generation scenarios)
     */
    public Promotion(Integer discountPercentage, PromotionStatus status, 
                    LocalDateTime startDate, LocalDateTime endDate) {
        this(null, discountPercentage, status, startDate, endDate);
    }

    /**
     * Creates a copy of this promotion with updated status.
     */
    public Promotion withStatus(PromotionStatus newStatus) {
        return new Promotion(
                this.code,
                this.discountPercentage,
                newStatus,
                this.startDate,
                this.endDate
        );
    }

    /**
     * Creates a copy of this promotion with updated dates.
     */
    public Promotion withDates(LocalDateTime newStartDate, LocalDateTime newEndDate) {
        return new Promotion(
                this.code,
                this.discountPercentage,
                this.status,
                newStartDate,
                newEndDate
        );
    }

    /**
     * Creates a copy of this promotion with updated discount percentage.
     */
    public Promotion withDiscountPercentage(Integer newDiscountPercentage) {
        return new Promotion(
                this.code,
                newDiscountPercentage,
                this.status,
                this.startDate,
                this.endDate
        );
    }

    /**
     * Checks if the promotion is currently valid (active and within date range).
     */
    public boolean isCurrentlyValid() {
        if (this.status != PromotionStatus.ACTIVE) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(this.startDate) && !now.isAfter(this.endDate);
    }

    /**
     * Checks if the promotion should be automatically activated.
     */
    public boolean shouldBeActivated() {
        if (this.status != PromotionStatus.SCHEDULED) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(this.startDate) && now.isBefore(this.endDate);
    }

    /**
     * Checks if the promotion should be automatically deactivated.
     */
    public boolean shouldBeDeactivated() {
        if (this.status != PromotionStatus.ACTIVE) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(this.endDate);
    }
}
