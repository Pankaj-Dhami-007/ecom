package com.dhami.cartservice.kafka;

import com.dhami.cartservice.cart.service.CartService;
import com.dhami.cartservice.kafka.dto.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final CartService cartService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            // names the record to build for this topic; every topic carries one event type
            properties = "spring.json.value.default.type=com.dhami.cartservice.kafka.dto.PaymentSucceededEvent"
    )
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received payment.succeeded | order id: {} | payment ref: {} | event id: {}",
                event.orderId(), event.paymentReference(), event.eventId());

        cartService.markCartConvertedForOrder(event.orderId());

        log.info("Finished handling payment.succeeded for order id: {} | event id: {}",
                event.orderId(), event.eventId());
    }
}