package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for trip collaboration details.
 * Shows collaboration information focused on the trip.
 */
@Data
public class GroupDetailsResponse {
    
    private String groupId; // Internal collaboration ID
    
    private String tripId; // The trip being collaborated on
    
    private String tripName; // Trip display name
    
    private String visibility; // private or public collaboration
    
    private List<String> collaboratorIds; // User IDs of collaborators
    
    private Map<String, Object> preferences;
    
    private Instant createdAt;
    
    private Instant lastUpdated;
}
