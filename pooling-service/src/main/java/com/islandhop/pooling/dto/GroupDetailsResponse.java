package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for group details.
 * Follows the same patterns as other response DTOs for consistency.
 */
@Data
public class GroupDetailsResponse {
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private String visibility;
    
    private List<String> userIds;
    
    private Map<String, Object> preferences;
    
    private Instant createdAt;
    
    private Instant lastUpdated;
}
