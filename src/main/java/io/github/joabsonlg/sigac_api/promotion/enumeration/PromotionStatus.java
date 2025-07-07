package io.github.joabsonlg.sigac_api.promotion.enumeration;

/**
 * Enum representing the possible statuses of a promotion in the system.
 */
public enum PromotionStatus {

    /**
     * The promotion is scheduled for future activation.
     */
    SCHEDULED,

    /**
     * The promotion is currently active and can be used.
     */
    ACTIVE,

    /**
     * The promotion is inactive and cannot be used.
     */
    INACTIVE
}
