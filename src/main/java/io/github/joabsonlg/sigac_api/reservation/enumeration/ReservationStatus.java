package io.github.joabsonlg.sigac_api.reservation.enumeration;

/**
 * Enum representing the possible statuses of a reservation in the system.
 */
public enum ReservationStatus {

    /**
     * The reservation is pending confirmation.
     */
    PENDING,

    /**
     * The reservation has been confirmed and is waiting to start.
     */
    CONFIRMED,

    /**
     * The reservation is in progress (vehicle in use).
     */
    IN_PROGRESS,

    /**
     * The reservation has been completed successfully.
     */
    COMPLETED,

    /**
     * The reservation has been cancelled.
     */
    CANCELLED
}
