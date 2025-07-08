package com.islandhop.trip.exception;

/**
 * Exception thrown when a user tries to access a trip they don't own.
 */
public class UnauthorizedTripAccessException extends RuntimeException {
    
    public UnauthorizedTripAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedTripAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
