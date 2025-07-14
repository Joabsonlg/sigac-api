package io.github.joabsonlg.sigac_api.payment.dto;

import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod;

import java.math.BigDecimal;

public record CreatePaymentDTO(
    Long reservationId,
    PaymentMethod paymentMethod,
    BigDecimal amount
) {
}
