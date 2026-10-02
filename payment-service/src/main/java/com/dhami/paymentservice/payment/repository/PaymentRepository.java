package com.dhami.paymentservice.payment.repository;

import com.dhami.paymentservice.payment.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    /**
     * An order can have several payment rows: one per attempt. A retry after a declined
     * payment adds a new row instead of overwriting the old one, so the failure history
     * is never lost.
     */
    Optional<PaymentEntity> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<PaymentEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
