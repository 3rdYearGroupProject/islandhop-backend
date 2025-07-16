package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for group details.
 * Shows group information including collaboration details.
 */
@Data
public class GroupDetailsResponse {
    
    private String groupId; // Internal group ID
    
    private String groupName; // Group name
    
    private String tripId; // The trip being collaborated on
    
    private String tripName; // Trip display name
    
    private String visibility; // private or public collaboration
    
    private List<String> userIds; // User IDs of members
    
    private List<String> collaboratorIds; // Alias for backward compatibility
    
    private Map<String, Object> preferences;
    
    private Instant createdAt;
    
    private Instant lastUpdated;
    
    // Convenience methods for backward compatibility
    public void setCollaboratorIds(List<String> collaboratorIds) {
        this.collaboratorIds = collaboratorIds;
        this.userIds = collaboratorIds;
    }
    
    public List<String> getCollaboratorIds() {
        return userIds;
    }
}
