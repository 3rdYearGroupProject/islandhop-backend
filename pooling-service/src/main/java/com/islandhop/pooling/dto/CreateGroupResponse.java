package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for group creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupResponse {
    
    private String status;
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private String message;
}
