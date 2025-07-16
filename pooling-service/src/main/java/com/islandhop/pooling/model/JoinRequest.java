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
    
    private String id; // Unique ID for this join request
    
    private String userId;
    
    private String userEmail;
    
    private String userName;
    
    private Map<String, Object> userProfile;
    
    private String status; // "pending", "approved", "rejected"
    
    private String message; // Optional message from user when requesting to join
    
    private String rejectionReason; // Reason for rejection if applicable
    
    private Instant requestedAt;
    
    private Instant respondedAt;
    
    private String reviewedByUserId; // ID of the user who approved/rejected
    
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
    public void approve(String reviewerUserId) {
        this.status = "approved";
        this.respondedAt = Instant.now();
        this.reviewedByUserId = reviewerUserId;
    }
    
    /**
     * Mark the join request as rejected.
     */
    public void reject(String reviewerUserId, String reason) {
        this.status = "rejected";
        this.respondedAt = Instant.now();
        this.reviewedByUserId = reviewerUserId;
        this.rejectionReason = reason;
    }
}
