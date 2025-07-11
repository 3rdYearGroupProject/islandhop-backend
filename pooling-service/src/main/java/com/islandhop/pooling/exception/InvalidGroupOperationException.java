package com.islandhop.pooling.exception;

/**
 * Exception thrown when an invalid group operation is attempted.
 */
public class InvalidGroupOperationException extends RuntimeException {
    
    public InvalidGroupOperationException(String message) {
        super(message);
    }
    
    public InvalidGroupOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
