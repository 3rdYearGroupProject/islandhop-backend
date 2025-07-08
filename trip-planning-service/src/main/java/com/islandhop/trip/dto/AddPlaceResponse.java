package com.islandhop.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for adding a place to an itinerary.
 * Contains confirmation details and the added place information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPlaceResponse {
    
    /**
     * Status of the operation (typically "success")
     */
    private String status;
    
    /**
     * Human-readable message describing the result
     */
    private String message;
    
    /**
     * ID of the place that was added
     */
    private String placeId;
    
    /**
     * Trip ID for reference
     */
    private String tripId;
    
    /**
     * Day number where the place was added
     */
    private Integer day;
    
    /**
     * Type of place that was added (attractions, hotels, restaurants)
     */
    private String type;
    
    /**
     * Constructor for simple success responses
     */
    public AddPlaceResponse(String status, String message, String placeId) {
        this.status = status;
        this.message = message;
        this.placeId = placeId;
    }
}
