package com.islandhop.pooling.exception;

/**
 * Exception thrown when a user is not authorized to access a trip.
 */
public class UnauthorizedTripAccessException extends RuntimeException {
    
    public UnauthorizedTripAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedTripAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
