package io.github.joabsonlg.sigac_api.payment.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus;
import io.github.joabsonlg.sigac_api.payment.model.Payment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public class PaymentRepository extends BaseRepository<Payment, Long> {

    public PaymentRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    protected String getTableName() {
        return "payment";
    }

    public Flux<Payment> findAll() {
        return databaseClient.sql("SELECT * FROM payment")
                .map((row, meta) -> new Payment(
                        row.get("id", Long.class),
                        row.get("reservation_id", Long.class),
                        row.get("payment_date", java.time.LocalDateTime.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                        row.get("amount", java.math.BigDecimal.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                )).all();
    }

    public Flux<Payment> findWithPagination(int page, int size) {
        return databaseClient.sql("SELECT * FROM payment" + createLimitOffset(page, size))
                .map((row, meta) -> new Payment(
                        row.get("id", Long.class),
                        row.get("reservation_id", Long.class),
                        row.get("payment_date", java.time.LocalDateTime.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                        row.get("amount", java.math.BigDecimal.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                )).all();
    }

    public Flux<Payment> findWithPaginationAndStatus(int page, int size, String status) {
        return databaseClient.sql("SELECT * FROM payment WHERE status = :status" + createLimitOffset(page, size))
                .bind("status", status)
                .map((row, meta) -> new Payment(
                        row.get("id", Long.class),
                        row.get("reservation_id", Long.class),
                        row.get("payment_date", java.time.LocalDateTime.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                        row.get("amount", java.math.BigDecimal.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                )).all();
    }

    public Mono<Payment> findById(Long id) {
        return databaseClient.sql("SELECT * FROM payment WHERE id = :id")
                .bind("id", id)
                .map((row, meta) -> new Payment(
                        row.get("id", Long.class),
                        row.get("reservation_id", Long.class),
                        row.get("payment_date", java.time.LocalDateTime.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                        row.get("amount", java.math.BigDecimal.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                )).one();
    }

    public Mono<Payment> save(Payment payment) {
        if (payment.id() == null) {
            DatabaseClient.GenericExecuteSpec spec = databaseClient.sql("INSERT INTO payment (reservation_id, payment_date, payment_method, amount, status) VALUES (:reservation_id, :payment_date, :payment_method, :amount, :status) RETURNING *")
                    .bind("reservation_id", payment.reservationId());

            if (payment.paymentDate() != null) {
                spec = spec.bind("payment_date", payment.paymentDate());
            } else {
                spec = spec.bindNull("payment_date", java.time.LocalDateTime.class);
            }

            return spec.bind("payment_method", payment.paymentMethod().name())
                    .bind("amount", payment.amount())
                    .bind("status", payment.status().name())
                    .map((row, meta) -> new Payment(
                            row.get("id", Long.class),
                            row.get("reservation_id", Long.class),
                            row.get("payment_date", java.time.LocalDateTime.class),
                            io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                            row.get("amount", java.math.BigDecimal.class),
                            io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                    )).one();
        } else {
            DatabaseClient.GenericExecuteSpec spec = databaseClient.sql("UPDATE payment SET reservation_id = :reservation_id, payment_date = :payment_date, payment_method = :payment_method, amount = :amount, status = :status WHERE id = :id RETURNING *")
                    .bind("id", payment.id())
                    .bind("reservation_id", payment.reservationId());

            if (payment.paymentDate() != null) {
                spec = spec.bind("payment_date", payment.paymentDate());
            } else {
                spec = spec.bindNull("payment_date", java.time.LocalDateTime.class);
            }

            return spec.bind("payment_method", payment.paymentMethod().name())
                    .bind("amount", payment.amount())
                    .bind("status", payment.status().name())
                    .map((row, meta) -> new Payment(
                            row.get("id", Long.class),
                            row.get("reservation_id", Long.class),
                            row.get("payment_date", java.time.LocalDateTime.class),
                            io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                            row.get("amount", java.math.BigDecimal.class),
                            io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                    )).one();
        }
    }

    public Mono<Payment> updateStatusAndPaymentDate(Long id, PaymentStatus status, LocalDateTime paymentDate) {
        return databaseClient.sql("UPDATE payment SET status = :status, payment_date = :payment_date WHERE id = :id RETURNING *")
                .bind("id", id)
                .bind("status", status.name())
                .bind("payment_date", paymentDate)
                .map((row, meta) -> new Payment(
                        row.get("id", Long.class),
                        row.get("reservation_id", Long.class),
                        row.get("payment_date", java.time.LocalDateTime.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentMethod.valueOf(row.get("payment_method", String.class)),
                        row.get("amount", java.math.BigDecimal.class),
                        io.github.joabsonlg.sigac_api.payment.enumeration.PaymentStatus.valueOf(row.get("status", String.class))
                )).one();
    }

    public Mono<Long> deleteByReservationId(Integer reservationId) {
        return databaseClient.sql("DELETE FROM payment WHERE reservation_id = :reservation_id")
                .bind("reservation_id", reservationId)
                .fetch()
                .rowsUpdated();
    }
    
    /**
     * Retorna o total de pagamentos recebidos (status PAID).
     * @return Mono<BigDecimal> receita bruta
     */
    public Mono<java.math.BigDecimal> calcularReceitaBruta() {
        return databaseClient.sql("SELECT COALESCE(SUM(amount), 0) FROM payment WHERE status = 'PAID'")
                .map(row -> row.get(0, java.math.BigDecimal.class))
                .one();
    }
}
