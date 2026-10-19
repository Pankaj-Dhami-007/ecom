package com.dhami.paymentservice.kafka;

public class OrderPaymentNotConcludedException extends RuntimeException {

    public OrderPaymentNotConcludedException(String message) {
        super(message);
    }
}