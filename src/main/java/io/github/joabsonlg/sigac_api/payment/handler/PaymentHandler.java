package io.github.joabsonlg.sigac_api.payment.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.payment.dto.CreatePaymentDTO;
import io.github.joabsonlg.sigac_api.payment.dto.PaymentDTO;
import io.github.joabsonlg.sigac_api.payment.dto.UpdatePaymentDTO;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;
import io.github.joabsonlg.sigac_api.payment.model.Payment;
import io.github.joabsonlg.sigac_api.payment.repository.PaymentRepository;
import io.github.joabsonlg.sigac_api.payment.validator.PaymentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PaymentHandler extends BaseHandler<Payment, PaymentDTO, Long> {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;

    public PaymentHandler(PaymentRepository paymentRepository, PaymentValidator paymentValidator) {
        this.paymentRepository = paymentRepository;
        this.paymentValidator = paymentValidator;
    }

    @Override
    protected PaymentDTO toDto(Payment entity) {
        return new PaymentDTO(
                entity.id(),
                entity.reservationId(),
                entity.paymentDate(),
                entity.paymentMethod(),
                entity.amount(),
                entity.status()
        );
    }

    @Override
    protected Payment toEntity(PaymentDTO dto) {
        return new Payment(
                dto.id(),
                dto.reservationId(),
                dto.paymentDate(),
                dto.paymentMethod(),
                dto.amount(),
                dto.status()
        );
    }

    public Flux<PaymentDTO> findAll() {
        return toDtoFlux(paymentRepository.findAll());
    }

    public Mono<PaymentDTO> findById(Long id) {
        return paymentRepository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment", id)));
    }

    public Mono<PageResponse<PaymentDTO>> getAllPaginated(int page, int size, String status) {
        Flux<PaymentDTO> payments;
        Mono<Long> totalElements;

        if (status != null && !status.isEmpty()) {
            payments = toDtoFlux(paymentRepository.findWithPaginationAndStatus(page, size, status));
            totalElements = paymentRepository.countWithCondition("status = '" + status + "'");
        } else {
            payments = toDtoFlux(paymentRepository.findWithPagination(page, size));
            totalElements = paymentRepository.count();
        }

        return createPageResponse(payments, page, size, totalElements);
    }

    @Transactional
    public Mono<PaymentDTO> create(CreatePaymentDTO dto) {
        paymentValidator.validateCreatePayment(dto);
        Payment payment = new Payment(
                null,
                dto.reservationId(),
                null,
                dto.paymentMethod(),
                dto.amount(),
                PaymentStatus.PENDING
        );
        return paymentRepository.save(payment)
                .map(this::toDto);
    }

    @Transactional
    public Mono<PaymentDTO> update(Long id, UpdatePaymentDTO dto) {
        paymentValidator.validateUpdatePayment(dto);
        return paymentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment", id)))
                .flatMap(existingPayment -> {
                    Payment updatedPayment = new Payment(
                            existingPayment.id(),
                            existingPayment.reservationId(),
                            existingPayment.paymentDate(),
                            existingPayment.paymentMethod(),
                            existingPayment.amount(),
                            dto.status()
                    );
                    return paymentRepository.save(updatedPayment);
                })
                .map(this::toDto);
    }

    @Transactional
    public Mono<PaymentDTO> updateStatus(Long id, PaymentStatus newStatus) {
        return paymentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment", id)))
                .flatMap(existingPayment -> {
                    LocalDateTime paymentDate = newStatus == PaymentStatus.PAID ? LocalDateTime.now() : existingPayment.paymentDate();
                    return paymentRepository.updateStatusAndPaymentDate(id, newStatus, paymentDate);
                })
                .map(this::toDto);
    }

    @Transactional
    public Mono<Void> delete(Long id) {
        return paymentRepository.existsById(id)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment", id)))
                .then(paymentRepository.deleteById(id));
    }

    @Transactional
    public Mono<Void> deleteByReservationId(Integer reservationId) {
        return paymentRepository.deleteByReservationId(reservationId)
                .flatMap(deletedCount -> {
                    if (deletedCount > 0) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new ResourceNotFoundException("Payment with reservation ID", reservationId));
                    }
                });
    }
}
