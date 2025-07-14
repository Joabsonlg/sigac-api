package io.github.joabsonlg.sigac_api.payment.dto;

import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;

public record UpdatePaymentDTO(
    PaymentStatus status
) {
}
