package com.islandhop.pooling.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MongoDB entity representing a travel group.
 * Stores group metadata and links to TripPlan via tripId.
 * Follows the same patterns as TripPlan entity for consistency.
 */
@Data
@Document(collection = "groups")
public class Group {
    
    @Id
    private String id;
    
    @Indexed
    private String tripId; // The trip this group is associated with
    
    private String groupName; // Display name for the group
    
    private List<String> userIds = new ArrayList<>();
    
    @Indexed
    private String visibility; // "private" or "public"
    
    @Indexed
    private Map<String, Object> preferences;
    
    private String status = "draft"; // "draft", "active", "finalized"
    
    private List<JoinRequest> joinRequests = new ArrayList<>();
    
    private List<GroupAction> actions = new ArrayList<>();
    
    private Instant createdAt;
    
    private Instant lastUpdated;
    
    private List<Invitation> pendingInvitations = new ArrayList<>();
    
    private boolean requiresApproval = true; // For public groups, whether join requests need approval
    
    private int maxMembers = 12; // Maximum number of members allowed
    
    /**
     * Get the creator's user ID (first user in the list).
     */
    public String getCreatorUserId() {
        return userIds.isEmpty() ? null : userIds.get(0);
    }
    
    /**
     * Check if a user is a member of this group.
     */
    public boolean isMember(String userId) {
        return userIds.contains(userId);
    }
    
    /**
     * Check if a user is the creator of this group.
     */
    public boolean isCreator(String userId) {
        return userId != null && userId.equals(getCreatorUserId());
    }
    
    /**
     * Add a user to the group.
     */
    public void addUser(String userId) {
        if (!userIds.contains(userId)) {
            userIds.add(userId);
        }
    }
    
    /**
     * Remove a user from the group.
     */
    public void removeUser(String userId) {
        userIds.remove(userId);
    }
    
    /**
     * Check if the group is public.
     */
    public boolean isPublic() {
        return "public".equals(visibility);
    }
    
    /**
     * Check if the group is private.
     */
    public boolean isPrivate() {
        return "private".equals(visibility);
    }
    
    /**
     * Check if the group is a draft (not finalized).
     */
    public boolean isDraft() {
        return "draft".equals(status);
    }
    
    /**
     * Check if the group is active.
     */
    public boolean isActive() {
        return "active".equals(status);
    }
    
    /**
     * Check if the group is finalized.
     */
    public boolean isFinalized() {
        return "finalized".equals(status);
    }
    
    /**
     * Mark the group as finalized.
     */
    public void finalize() {
        this.status = "finalized";
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Mark the group as active.
     */
    public void activate() {
        this.status = "active";
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Check if the group is full.
     */
    public boolean isFull() {
        return userIds.size() >= maxMembers;
    }
    
    /**
     * Get pending join request by user ID.
     */
    public JoinRequest getPendingJoinRequest(String userId) {
        return joinRequests.stream()
                .filter(request -> request.getUserId().equals(userId) && request.isPending())
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if user has a pending join request.
     */
    public boolean hasPendingJoinRequest(String userId) {
        return getPendingJoinRequest(userId) != null;
    }
    
    /**
     * Add a join request.
     */
    public void addJoinRequest(JoinRequest joinRequest) {
        // Remove any existing requests from the same user
        joinRequests.removeIf(request -> request.getUserId().equals(joinRequest.getUserId()));
        joinRequests.add(joinRequest);
    }
    
    /**
     * Add a pending invitation.
     */
    public void addPendingInvitation(Invitation invitation) {
        pendingInvitations.add(invitation);
    }
    
    /**
     * Remove a pending invitation.
     */
    public void removePendingInvitation(String invitationId) {
        pendingInvitations.removeIf(invitation -> invitation.getId().equals(invitationId));
    }
}
