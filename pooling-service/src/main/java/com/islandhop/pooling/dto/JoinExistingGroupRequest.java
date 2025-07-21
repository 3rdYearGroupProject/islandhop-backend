package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Request DTO for joining an existing group.
 */
@Data
public class JoinExistingGroupRequest {
    private String groupId;
    private String targetGroupId; // Add this alias for backwards compatibility
    private String userId;
    
    // Getter for backward compatibility
    public String getTargetGroupId() {
        return targetGroupId != null ? targetGroupId : groupId;
    }
}
