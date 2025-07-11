package com.islandhop.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO specifically for suggestion-related endpoints.
 * Provides consistent error formatting for client applications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionErrorResponse {
    
    /**
     * Status of the response (typically "error")
     */
    private String status;
    
    /**
     * Trip ID if available
     */
    private String tripId;
    
    /**
     * Day number if applicable
     */
    private Integer day;
    
    /**
     * Suggestion type if applicable
     */
    private String type;
    
    /**
     * User-friendly error message
     */
    private String message;
    
    /**
     * Constructor for simple error responses without context
     */
    public SuggestionErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    

}
