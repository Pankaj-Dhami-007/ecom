package com.dhami.paymentservice.kafka;

import com.dhami.paymentservice.kafka.dto.OrderCreatedEvent;
import com.dhami.paymentservice.payment.dto.CreatePaymentRequest;
import com.dhami.paymentservice.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Consumes ORDER_CREATED events from Kafka and initiates payment processing.
// Listens to the ORDER_CREATED Kafka topic.
// Receives order details through OrderCreatedEvent.
// Creates a CreatePaymentRequest using the event data.
// Calls PaymentService to process the payment.
// Throws an exception if the payment attempt is not concluded,
// allowing Kafka message retry according to consumer configuration.
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private static final String DEFAULT_CURRENCY = "INR";

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received ORDER_CREATED for order id: {} number: {} amount: {}",
                event.orderId(), event.orderNumber(), event.totalAmount());

        PaymentService.CreatePaymentResult result = paymentService.createPayment(new CreatePaymentRequest(
                event.orderId(),
                event.customerId(),
                null,// // not carried on this event, and payment-service does not need it.
                event.totalAmount(),
                DEFAULT_CURRENCY
        ));

        if (!result.charged()) {
            log.warn("Order id: {} payment attempt did not conclude ({}) - retrying the event",
                    event.orderId(), result.message());
            throw new OrderPaymentNotConcludedException("Gateway did not answer for order id: " + event.orderId());
        }
        log.info("Order id: {} settled as {}", event.orderId(), result.status());
    }
}

// @KafkaListener(topics = KafkaTopics.ORDER_CREATED)

// Processes an ORDER_CREATED event received from Kafka.
// Creates a CreatePaymentRequest using the order and customer details.
// Calls PaymentService.createPayment() to initiate payment processing.