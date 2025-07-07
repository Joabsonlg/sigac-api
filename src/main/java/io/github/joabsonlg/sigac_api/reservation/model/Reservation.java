package io.github.joabsonlg.sigac_api.reservation.model;

import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity model representing a Reservation in the system.
 * Manages rental reservations for vehicles by clients.
 */
@Table("reservation")
public record Reservation(
        @Id
        @Column("id")
        Integer id,

        @Column("start_date")
        LocalDateTime startDate,

        @Column("end_date")
        LocalDateTime endDate,

        @Column("reservation_date")
        LocalDateTime reservationDate,

        @Column("status")
        ReservationStatus status,

        @Column("promotion_code")
        Integer promotionCode,

        @Column("client_user_cpf")
        String clientUserCpf,

        @Column("employee_user_cpf")
        String employeeUserCpf,

        @Column("vehicle_plate")
        String vehiclePlate
) {
    /**
     * Creates a copy of this reservation with updated status.
     */
    public Reservation withStatus(ReservationStatus newStatus) {
        return new Reservation(
                this.id,
                this.startDate,
                this.endDate,
                this.reservationDate,
                newStatus,
                this.promotionCode,
                this.clientUserCpf,
                this.employeeUserCpf,
                this.vehiclePlate
        );
    }

    /**
     * Creates a copy of this reservation with updated dates.
     */
    public Reservation withDates(LocalDateTime startDate, LocalDateTime endDate) {
        return new Reservation(
                this.id,
                startDate,
                endDate,
                this.reservationDate,
                this.status,
                this.promotionCode,
                this.clientUserCpf,
                this.employeeUserCpf,
                this.vehiclePlate
        );
    }
}
