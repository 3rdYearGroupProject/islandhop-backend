package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for public group listing.
 * Follows the same patterns as other response DTOs for consistency.
 */
@Data
public class PublicGroupResponse {
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private Map<String, Object> preferences;
    
    private int memberCount;
    
    private Instant createdAt;
}
