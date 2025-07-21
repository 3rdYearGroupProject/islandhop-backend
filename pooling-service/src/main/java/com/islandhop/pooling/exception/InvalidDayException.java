package com.islandhop.pooling.exception;

/**
 * Exception thrown when invalid day is provided.
 */
public class InvalidDayException extends RuntimeException {
    
    public InvalidDayException(String message) {
        super(message);
    }
    
    public InvalidDayException(String message, Throwable cause) {
        super(message, cause);
    }
}
