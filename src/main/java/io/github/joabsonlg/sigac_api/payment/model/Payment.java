package io.github.joabsonlg.sigac_api.payment.model;

import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("payments")
public record Payment(
    @Id Long id,
    @Column("reservation_id") Long reservationId,
    @Column("payment_date") LocalDateTime paymentDate,
    @Column("payment_method") PaymentMethod paymentMethod,
    @Column("amount") BigDecimal amount,
    @Column("status") PaymentStatus status
) {
}
