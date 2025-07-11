package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for group itinerary operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupItineraryResponse {
    
    private String status;
    
    private String groupId;
    
    private String tripId;
    
    private String message;
}
