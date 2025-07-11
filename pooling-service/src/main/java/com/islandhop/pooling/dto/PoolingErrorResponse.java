package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO for pooling service endpoints.
 * Maintains consistency with the itinerary service's SuggestionErrorResponse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolingErrorResponse {
    
    private String status;
    
    private String groupId;
    
    private String tripId;
    
    private String message;
    
    /**
     * Constructor for simple error responses.
     */
    public PoolingErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    /**
     * Constructor for group-specific error responses.
     */
    public PoolingErrorResponse(String status, String groupId, String message) {
        this.status = status;
        this.groupId = groupId;
        this.message = message;
    }
}
