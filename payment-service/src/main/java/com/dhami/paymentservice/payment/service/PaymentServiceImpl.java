package com.dhami.paymentservice.payment.service;

import com.dhami.paymentservice.common.exception.PaymentNotFoundException;
import com.dhami.paymentservice.kafka.PaymentEventPublisher;
import com.dhami.paymentservice.payment.PaymentEntity;
import com.dhami.paymentservice.payment.PaymentStatus;
import com.dhami.paymentservice.payment.dto.CreatePaymentRequest;
import com.dhami.paymentservice.payment.dto.PaymentResponse;
import com.dhami.paymentservice.payment.gateway.GatewayUnavailableException;
import com.dhami.paymentservice.payment.gateway.PaymentGateway;
import com.dhami.paymentservice.payment.gateway.PaymentGatewayRequest;
import com.dhami.paymentservice.payment.gateway.PaymentGatewayResult;
import com.dhami.paymentservice.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final String DEFAULT_CURRENCY = "INR";

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    // private final PaymentCallbackService paymentCallbackService;

    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public CreatePaymentResult createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for order id: {} amount: {} {}",
                request.orderId(), request.amount(), currencyOrDefault(request.currency()));

        PaymentEntity latest = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(request.orderId())
                .orElse(null);

        if (latest != null && latest.getStatus() == PaymentStatus.SUCCESS) {
            // the one hard guard: money definitely moved, so never charge this order again
            log.warn("Order id: {} is already paid - returning the successful payment without charging again",
                    request.orderId());
            return new CreatePaymentResult(true, PaymentStatus.SUCCESS,
                    "Order is already paid, no new charge attempted");
        }

        if (latest != null) {
            log.info("Previous attempt for order id: {} was {} - starting a new attempt",
                    request.orderId(), latest.getStatus());
        }

        PaymentEntity payment = PaymentEntity.builder()
                .paymentReference(generateReference())
                .orderId(request.orderId())
                .cartId(request.cartId())
                .customerId(request.customerId())
                .amount(request.amount())
                .currency(currencyOrDefault(request.currency()))
                .status(PaymentStatus.PENDING)
                .gateway(paymentGateway.name())
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        PaymentGatewayResult result;
        try {
            result = paymentGateway.charge(new PaymentGatewayRequest(
                    saved.getPaymentReference(),
                    saved.getOrderId(),
                    saved.getAmount(),
                    saved.getCurrency()
            ));
        } catch (GatewayUnavailableException e) {
            // leave the payment PENDING: the order keeps its reservation and the user can retry
            log.error("Gateway {} did not answer for payment {} - payment stays PENDING, reservation held",
                    paymentGateway.name(), saved.getPaymentReference(), e);
            // charged=false is what tells the caller this is unknown rather than declined.
            return new CreatePaymentResult(false, PaymentStatus.PENDING,
                    "Payment gateway did not respond, payment is still pending");
        }

        if (result.approved()) {
            saved.markSuccess(result.gatewayReference());
            paymentRepository.save(saved);
            log.info("Payment {} approved by gateway {}", saved.getPaymentReference(), paymentGateway.name());

            // OLD: synchronous callback into order-service.
            // paymentCallbackService.onPaymentSuccess(saved);

            paymentEventPublisher.publishSucceeded(saved);
            return new CreatePaymentResult(true, PaymentStatus.SUCCESS, "Payment completed successfully");
        }

        saved.markFailed(result.failureReason());
        paymentRepository.save(saved);
        log.info("Payment {} declined by gateway {}: {}",
                saved.getPaymentReference(), paymentGateway.name(), result.failureReason());

        // OLD: synchronous callback into order-service.
        // paymentCallbackService.onPaymentFailed(saved);

        paymentEventPublisher.publishFailed(saved);
        return new CreatePaymentResult(true, PaymentStatus.FAILED, "Payment failed");
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.info("Fetching latest payment for order id: {}", orderId);
        PaymentEntity payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for order id: " + orderId));
        return toResponse(payment, null);
    }

    @Override
    public List<PaymentResponse> getPaymentsByCustomerId(Long customerId) {
        log.info("Fetching payments for customer id: {}", customerId);
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(payment -> toResponse(payment, null))
                .toList();
    }

    private String generateReference() {
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String currencyOrDefault(String currency) {
        return (currency == null || currency.isBlank()) ? DEFAULT_CURRENCY : currency;
    }

    private PaymentResponse toResponse(PaymentEntity payment, String message) {
        return new PaymentResponse(
                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getCartId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getGateway(),
                payment.getGatewayReference(),
                message,
                payment.getCreatedAt()
        );
    }
}
