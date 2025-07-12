package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Response DTO for group creation.
 * Follows the same patterns as CreateTripResponse for consistency.
 */
@Data
public class CreateGroupResponse {
    
    private String status;
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private String message;
}
