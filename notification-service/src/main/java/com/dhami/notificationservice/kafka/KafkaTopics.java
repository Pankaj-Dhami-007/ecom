package com.dhami.notificationservice.kafka;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String ORDER_DELIVERED = "order.delivered";

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";

    private KafkaTopics() {
        throw new AssertionError("KafkaTopics only holds constants and must not be instantiated");
    }
}