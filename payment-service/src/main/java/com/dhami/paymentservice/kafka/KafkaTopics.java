package com.dhami.paymentservice.kafka;

// Defines the Kafka topic names used by the payment service.
public final class KafkaTopics {

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";

    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String ORDER_CREATED = "order.created";

    private KafkaTopics() {
        throw new AssertionError("KafkaTopics only holds constants and must not be instantiated");
    }
}