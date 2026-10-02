package com.dhami.orderservice.order;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
