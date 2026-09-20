package com.dhami.ecom.notification_service.exception;

public class InvalidEmailRequestException extends RuntimeException {

    public InvalidEmailRequestException(String message) {
        super(message);
    }

    public InvalidEmailRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
