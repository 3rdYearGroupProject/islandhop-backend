package com.islandhop.trip.exception;

/**
 * Exception thrown when an invalid day number is provided for a trip.
 */
public class InvalidDayException extends RuntimeException {
    
    public InvalidDayException(String message) {
        super(message);
    }
    
    public InvalidDayException(String message, Throwable cause) {
        super(message, cause);
    }
}
