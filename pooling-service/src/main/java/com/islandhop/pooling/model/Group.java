package com.islandhop.pooling.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
    
    private String groupName; // Name of the group
    
    private String tripName; // Name of the trip
    
    private String creatorUserId; // User who created the group
    
    private String creatorEmail; // Email of the user who created the group (for user name lookup)
    
    private String createdBy; // Alias for creatorUserId for backward compatibility
        
    private List<String> userIds = new ArrayList<>(); // List of user IDs in the group
    
    private List<Member> members = new ArrayList<>(); // Detailed member information including creator
    
    @Indexed
    private String visibility; // "private" or "public"
    
    @Indexed
    private Map<String, Object> preferences; // Group preferences (e.g., trip type, budget)
    
    private String status = "draft"; // "draft", "active", "finalized"
    
    private List<JoinRequest> joinRequests = new ArrayList<>();
    
    private List<GroupAction> actions = new ArrayList<>();
    
    private Instant createdAt;
    
    private Instant lastUpdated;
    
    private Instant finalizedAt; // When the group was finalized
    
    private String finalizedBy; // User ID who finalized the group
    
    private List<Invitation> pendingInvitations = new ArrayList<>();
    
    private boolean requiresApproval = true; // For public groups, whether join requests need approval
    
    private int maxMembers = 12; // Maximum number of members allowed
    
    // Trip logistics and cost details (added for finalization)
    private Double averageDriverCost;
    
    private Double averageGuideCost;
    
    private Double totalCost;
    
    private Double costPerPerson;
    
    private Integer maxParticipants;
    
    private String vehicleType;
    
    private Boolean needDriver;
    
    private Boolean needGuide;
    
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
     * Checks both creatorUserId and createdBy for backward compatibility.
     */
    public boolean isCreator(String userId) {
        if (userId == null) {
            return false;
        }
        // Check both fields for backward compatibility
        return userId.equals(getCreatorUserId()) || userId.equals(getCreatedBy());
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
        this.finalizedAt = Instant.now();
    }
    
    /**
     * Mark the group as finalized by a specific user.
     */
    public void finalizeBy(String userId) {
        this.status = "finalized";
        this.lastUpdated = Instant.now();
        this.finalizedAt = Instant.now();
        this.finalizedBy = userId;
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
    
    /**
     * Backward compatibility methods
     */
    public String getCreatedBy() {
        return createdBy != null ? createdBy : creatorUserId;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        if (this.creatorUserId == null) {
            this.creatorUserId = createdBy;
        }
    }
    
    /**
     * Helper methods for member management
     */
    public void addMember(Member member) {
        if (!userIds.contains(member.getUserId())) {
            userIds.add(member.getUserId());
        }
        
        // Remove existing member with same userId and add new one
        members.removeIf(m -> m.getUserId().equals(member.getUserId()));
        members.add(member);
    }
    
    public Member getMemberByUserId(String userId) {
        return members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Embedded class representing a group member with full profile details.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Member {
        private String userId;
        private String email;
        private String firstName;
        private String lastName;
        private String nationality;
        private List<String> languages;
        private String dob; // Date of birth (e.g., "2000-05-15")
        private int profileCompletion; // Profile completion percentage (e.g., 85)
        private Instant joinedAt;
        private boolean isCreator;
        
        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return email != null ? email : userId;
        }
        
        public static Member createFromUserProfile(String userId, 
                                                  String email, 
                                                  String firstName, 
                                                  String lastName, 
                                                  String nationality, 
                                                  List<String> languages,
                                                  String dob,
                                                  int profileCompletion,
                                                  boolean isCreator) {
            Member member = new Member();
            member.setUserId(userId);
            member.setEmail(email);
            member.setFirstName(firstName);
            member.setLastName(lastName);
            member.setNationality(nationality);
            member.setLanguages(languages);
            member.setDob(dob);
            member.setProfileCompletion(profileCompletion);
            member.setJoinedAt(Instant.now());
            member.setCreator(isCreator);
            return member;
        }
    }
}
