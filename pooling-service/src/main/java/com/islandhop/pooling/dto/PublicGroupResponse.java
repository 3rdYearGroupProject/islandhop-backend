package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for public trip collaboration listing.
 * Shows trips available for collaboration, not group details.
 */
@Data
public class PublicGroupResponse {
    
    private String groupId; // Internal collaboration ID
    
    private String tripId; // The trip available for collaboration
    
    private String tripName; // Trip display name
    
    private Map<String, Object> preferences;
    
    private int collaboratorCount; // Number of people collaborating
    
    private Instant createdAt;
}
