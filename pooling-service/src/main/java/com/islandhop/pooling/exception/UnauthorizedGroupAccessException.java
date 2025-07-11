package com.islandhop.pooling.exception;

/**
 * Exception thrown when a user tries to access a group they don't have permission to access.
 */
public class UnauthorizedGroupAccessException extends RuntimeException {
    
    public UnauthorizedGroupAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedGroupAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
