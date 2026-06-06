package com.serviceonwheels.auth_service.exception;

/**
 * Thrown when the application fails to deliver an email via SMTP.
 * Handled by {@link GlobalExceptionHandler} to return HTTP 503.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message) {
        super(message);
    }

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
