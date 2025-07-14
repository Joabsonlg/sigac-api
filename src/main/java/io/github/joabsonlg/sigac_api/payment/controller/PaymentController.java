package io.github.joabsonlg.sigac_api.payment.controller;

import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.payment.dto.CreatePaymentDTO;
import io.github.joabsonlg.sigac_api.payment.dto.PaymentDTO;
import io.github.joabsonlg.sigac_api.payment.dto.UpdatePaymentDTO;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;
import io.github.joabsonlg.sigac_api.payment.handler.PaymentHandler;
import io.github.joabsonlg.sigac_api.reservation.enumeration.ReservationStatus;
import io.github.joabsonlg.sigac_api.reservation.handler.ReservationHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
public class PaymentController extends BaseController<PaymentDTO, Long> {

    private final PaymentHandler paymentHandler;
    private final ReservationHandler reservationHandler;

    public PaymentController(PaymentHandler paymentHandler, ReservationHandler reservationHandler) {
        this.paymentHandler = paymentHandler;
        this.reservationHandler = reservationHandler;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PageResponse<PaymentDTO>>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PaginationParams params = validatePagination(page, size);
        return okPage(paymentHandler.getAllPaginated(params.page(), params.size(), status));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<PaymentDTO>>> findById(@PathVariable Long id) {
        return ok(paymentHandler.findById(id));
    }


    @PostMapping
    public Mono<ResponseEntity<ApiResponse<PaymentDTO>>> create(@RequestBody CreatePaymentDTO dto) {
        return created(paymentHandler.create(dto));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<PaymentDTO>>> update(@PathVariable Long id, @RequestBody UpdatePaymentDTO dto) {
        return ok(paymentHandler.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<ApiResponse<PaymentDTO>>> updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return paymentHandler.updateStatus(id, status)
                .flatMap(paymentDTO -> {
                    if (paymentDTO.status() == PaymentStatus.PAID) {
                        return reservationHandler.updateStatus(paymentDTO.reservationId().intValue(), ReservationStatus.CONFIRMED)
                                .then(Mono.just(paymentDTO));
                    }
                    return Mono.just(paymentDTO);
                })
                .flatMap(finalPaymentDTO -> ok(Mono.just(finalPaymentDTO)));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return noContent();
    }
}
