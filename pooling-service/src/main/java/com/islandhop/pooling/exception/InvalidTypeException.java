package com.islandhop.pooling.exception;

/**
 * Exception thrown when invalid type is provided.
 */
public class InvalidTypeException extends RuntimeException {
    
    public InvalidTypeException(String message) {
        super(message);
    }
    
    public InvalidTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
