package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Response DTO for trip collaboration creation.
 * Users see this as starting trip collaboration, not creating groups.
 */
@Data
public class CreateGroupResponse {
    
    private String status;
    
    private String groupId; // Internal ID for collaboration
    
    private String tripId; // The trip being collaborated on
    
    private String message;
}
