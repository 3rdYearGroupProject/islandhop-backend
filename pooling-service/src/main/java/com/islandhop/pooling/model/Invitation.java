package com.islandhop.pooling.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;

/**
 * Represents an invitation sent to a user to join a private group.
 * Separate from JoinRequest (which is for public groups).
 */
@Data
public class Invitation {
    
    @Id
    private String id;
    
    private String groupId; // Internal group ID (not exposed to users)
    
    private String tripId; // The trip users will see
    
    private String tripName; // Display name for the trip
    
    private String groupName; // Group name for display
    
    private String inviterUserId;
    
    private String inviterEmail;
    
    private String inviterName;
    
    private String invitedUserId;
    
    private String invitedEmail;
    
    private String status; // "pending", "accepted", "rejected", "expired"
    
    private String message; // Optional message from inviter
    
    private Instant invitedAt;
    
    private Instant respondedAt;
    
    private Instant expiresAt;
    
    /**
     * Check if the invitation is pending.
     */
    public boolean isPending() {
        return "pending".equals(status) && !isExpired();
    }
    
    /**
     * Check if the invitation is accepted.
     */
    public boolean isAccepted() {
        return "accepted".equals(status);
    }
    
    /**
     * Check if the invitation is rejected.
     */
    public boolean isRejected() {
        return "rejected".equals(status);
    }
    
    /**
     * Check if the invitation is expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Accept the invitation.
     */
    public void accept() {
        this.status = "accepted";
        this.respondedAt = Instant.now();
    }
    
    /**
     * Reject the invitation.
     */
    public void reject() {
        this.status = "rejected";
        this.respondedAt = Instant.now();
    }
    
    /**
     * Mark the invitation as expired.
     */
    public void markExpired() {
        this.status = "expired";
    }
}
