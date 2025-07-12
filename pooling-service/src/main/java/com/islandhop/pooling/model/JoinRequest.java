package com.islandhop.pooling.model;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a join request for a public group.
 * Nested within Group entity.
 * Follows consistent patterns with other model classes.
 */
@Data
public class JoinRequest {
    
    private String userId;
    
    private Map<String, Object> userProfile;
    
    private String status; // "pending", "approved", "rejected"
    
    private Instant requestedAt;
    
    private Instant respondedAt;
    
    /**
     * Check if the join request is pending.
     */
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    /**
     * Check if the join request is approved.
     */
    public boolean isApproved() {
        return "approved".equals(status);
    }
    
    /**
     * Check if the join request is rejected.
     */
    public boolean isRejected() {
        return "rejected".equals(status);
    }
    
    /**
     * Mark the join request as approved.
     */
    public void approve() {
        this.status = "approved";
        this.respondedAt = java.time.Instant.now();
    }
    
    /**
     * Mark the join request as rejected.
     */
    public void reject() {
        this.status = "rejected";
        this.respondedAt = java.time.Instant.now();
    }
}
