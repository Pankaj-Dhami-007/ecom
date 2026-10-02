package com.dhami.paymentservice.payment;

import com.dhami.paymentservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // our own id, stable across gateway swaps
    @Column(nullable = false, unique = true, length = 40)
    private String paymentReference;

    @Column(nullable = false)
    private Long orderId;

    // nullable because an order can be created without going through a cart
    private Long cartId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    // which gateway handled this payment, so history stays readable after a swap
    @Column(nullable = false, length = 30)
    private String gateway;

    // the id the gateway gave us, e.g. DUMMY_TXN_xxx or a real provider txn id
    @Column(length = 80)
    private String gatewayReference;

    @Column(length = 255)
    private String failureReason;

    public void markSuccess(String gatewayReference) {
        this.status = PaymentStatus.SUCCESS;
        this.gatewayReference = gatewayReference;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }
}
