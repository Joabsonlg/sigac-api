package io.github.joabsonlg.sigac_api.payment.validator;

import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import io.github.joabsonlg.sigac_api.payment.dto.CreatePaymentDTO;
import io.github.joabsonlg.sigac_api.payment.dto.UpdatePaymentDTO;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {

    private final CommonValidator commonValidator;

    public PaymentValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }

    public void validateCreatePayment(CreatePaymentDTO dto) {
        commonValidator.validateRequired(String.valueOf(dto.reservationId()), "reservationId");
        commonValidator.validateRequired(String.valueOf(dto.paymentMethod()), "paymentMethod");
        commonValidator.validateRequired(String.valueOf(dto.amount()), "amount");
        commonValidator.validatePositive(dto.amount(), "amount");
    }

    public void validateUpdatePayment(UpdatePaymentDTO dto) {
        commonValidator.validateRequired(String.valueOf(dto.status()), "status");
    }
}
