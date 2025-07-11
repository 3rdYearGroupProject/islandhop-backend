package com.islandhop.pooling.exception;

/**
 * Exception thrown when group itinerary operations fail.
 */
public class GroupItineraryException extends RuntimeException {
    
    public GroupItineraryException(String message) {
        super(message);
    }
    
    public GroupItineraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
