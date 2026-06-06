package com.serviceonwheels.auth_service.exception;

/**
 * Thrown when a password reset token is invalid, expired, or already used.
 * Handled by {@link GlobalExceptionHandler} to return HTTP 400.
 */
public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException(String message) {
        super(message);
    }

    public InvalidResetTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
