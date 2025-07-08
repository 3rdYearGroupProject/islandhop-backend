package com.islandhop.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for city update operations.
 * Used by the POST /api/v1/itinerary/{tripId}/day/{day}/city endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCityResponse {
    
    /**
     * Status of the operation ("success" or "error")
     */
    private String status;
    
    /**
     * Trip ID that was updated
     */
    private String tripId;
    
    /**
     * Day number that was updated
     */
    private Integer day;
    
    /**
     * City that was set
     */
    private String city;
    
    /**
     * Descriptive message about the operation result
     */
    private String message;
    
    /**
     * Constructor for error responses
     */
    public UpdateCityResponse(String status, String message) {
        this.status = status;
        this.tripId = null;
        this.day = null;
        this.city = null;
        this.message = message;
    }
}
