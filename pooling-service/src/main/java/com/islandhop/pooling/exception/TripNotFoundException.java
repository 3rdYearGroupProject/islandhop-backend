package com.islandhop.pooling.exception;

/**
 * Exception thrown when a trip is not found.
 */
public class TripNotFoundException extends RuntimeException {
    
    public TripNotFoundException(String message) {
        super(message);
    }
    
    public TripNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
