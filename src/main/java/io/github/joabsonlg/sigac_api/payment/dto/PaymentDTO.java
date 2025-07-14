package io.github.joabsonlg.sigac_api.payment.dto;

import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(
    Long id,
    Long reservationId,
    LocalDateTime paymentDate,
    PaymentMethod paymentMethod,
    BigDecimal amount,
    PaymentStatus status
) {
}
