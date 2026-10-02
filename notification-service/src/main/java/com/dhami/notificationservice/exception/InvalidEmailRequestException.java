package com.dhami.notificationservice.exception;

public class InvalidEmailRequestException extends RuntimeException {

    public InvalidEmailRequestException(String message) {
        super(message);
    }

    public InvalidEmailRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
