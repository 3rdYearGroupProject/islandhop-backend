package com.islandhop.pooling.exception;

/**
 * Exception thrown when a join request is not found.
 */
public class JoinRequestNotFoundException extends RuntimeException {
    
    public JoinRequestNotFoundException(String message) {
        super(message);
    }
    
    public JoinRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
