package com.islandhop.trip.exception;

/**
 * Exception thrown when an invalid suggestion type is provided.
 * Valid types are: attractions, hotels, restaurants.
 */
public class InvalidTypeException extends RuntimeException {
    
    public InvalidTypeException(String message) {
        super(message);
    }
    
    public InvalidTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
