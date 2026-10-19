package com.dhami.cartservice.kafka;

public final class KafkaTopics {

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";

    private KafkaTopics() {
        throw new AssertionError("KafkaTopics only holds constants and must not be instantiated");
    }
}