package com.dhami.paymentservice.payment.service;

import com.dhami.paymentservice.client.order.OrderClient;
import com.dhami.paymentservice.payment.PaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final OrderClient orderClient;

    public void onPaymentSuccess(PaymentEntity payment) {
        log.info("Payment {} succeeded - confirming order id: {}",
                payment.getPaymentReference(), payment.getOrderId());

        try {
            orderClient.confirmOrder(payment.getOrderId(), payment.getCustomerId());
            log.info("Order {} confirmed and reserved stock marked as sold", payment.getOrderId());
        } catch (Exception e) {
            log.error("Payment {} succeeded but order {} could not be confirmed - manual reconciliation needed",
                    payment.getPaymentReference(), payment.getOrderId(), e);
        }
    }

    public void onPaymentFailed(PaymentEntity payment) {
        log.info("Payment {} failed - notifying order service for order id: {}",
                payment.getPaymentReference(), payment.getOrderId());

        try {
            orderClient.markPaymentFailed(payment.getOrderId(), payment.getCustomerId());
            log.info("Order {} moved to PAYMENT_FAILED and reserved stock released", payment.getOrderId());
        } catch (Exception e) {
            log.error("Payment {} failed but order {} could not be updated - manual reconciliation needed",
                    payment.getPaymentReference(), payment.getOrderId(), e);
        }
    }
}
