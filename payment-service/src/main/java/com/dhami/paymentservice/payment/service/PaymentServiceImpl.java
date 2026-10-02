package com.dhami.paymentservice.payment.service;

import com.dhami.paymentservice.common.exception.PaymentNotFoundException;
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
    private final PaymentCallbackService paymentCallbackService;

    /**
     * Charges the order and pushes the result back to order-service and cart-service.
     *
     * Deliberately not @Transactional: the gateway call is a slow external call that must
     * not sit inside a database transaction holding a lock on the payment row. The PENDING
     * row is committed first, so a crash mid-charge leaves an honest "we do not know yet"
     * record rather than a lost payment.
     */
    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for order id: {} amount: {} {}",
                request.orderId(), request.amount(), currencyOrDefault(request.currency()));

        PaymentEntity latest = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(request.orderId())
                .orElse(null);

        if (latest != null && latest.getStatus() == PaymentStatus.SUCCESS) {
            // the one hard guard: money definitely moved, so never charge this order again
            log.warn("Order id: {} is already paid - returning the successful payment without charging again",
                    request.orderId());
            return toResponse(latest, "Order is already paid, no new charge attempted");
        }

        // FAILED and PENDING both start a fresh attempt, which becomes a new row so the
        // earlier attempt stays in the history.
        //
        // WARNING for a real gateway: re-attempting a PENDING charge is only safe because
        // the dummy never actually moved money when it reported a timeout. Against a live
        // provider the outcome of a timed-out attempt is unknown, so this branch must
        // first ask the provider (getPaymentByOrderId) and only re-charge when the
        // provider confirms nothing was captured. See the unused getPaymentByOrderId below.
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
            return toResponse(saved, "Payment gateway did not respond, payment is still pending");
        }

        if (result.approved()) {
            saved.markSuccess(result.gatewayReference());
            paymentRepository.save(saved);
            log.info("Payment {} approved by gateway {}", saved.getPaymentReference(), paymentGateway.name());
            paymentCallbackService.onPaymentSuccess(saved);
            return toResponse(saved, "Payment completed successfully");
        }

        saved.markFailed(result.failureReason());
        paymentRepository.save(saved);
        log.info("Payment {} declined by gateway {}: {}",
                saved.getPaymentReference(), paymentGateway.name(), result.failureReason());
        paymentCallbackService.onPaymentFailed(saved);
        return toResponse(saved, "Payment failed");
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
