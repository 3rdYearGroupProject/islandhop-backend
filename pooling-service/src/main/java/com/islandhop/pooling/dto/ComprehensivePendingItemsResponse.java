package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Comprehensive response DTO that combines ALL pending items requiring user action.
 * Includes both invitations received and join requests that need voting.
 */
@Data
public class ComprehensivePendingItemsResponse {
    
    private String status;
    private String message;
    private int totalPendingItems;
    
    // Invitations the user has received (to join groups)
    private List<PendingInvitation> pendingInvitations;
    private int totalInvitations;
    
    // Join requests that need the user's vote (for groups they're a member of)
    private List<PendingVoteRequest> pendingVotes;
    private int totalVoteRequests;
    
    @Data
    public static class PendingInvitation {
        private String invitationId;
        private String groupId;
        private String groupName;
        private String tripName;
        private String inviterName;
        private String inviterEmail;
        private String message;
        private Instant invitedAt;
        private Instant expiresAt;
        
        // Group details
        private int currentMembers;
        private int maxMembers;
        private String baseCity;
        private Instant tripStartDate;
        private Instant tripEndDate;
        private List<String> preferredActivities;
        
        // Urgency indicator
        private String urgencyLevel; // "high", "medium", "low"
        private int daysRemaining;
    }
    
    @Data
    public static class PendingVoteRequest {
        private String joinRequestId;
        private String groupId;
        private String groupName;
        private String tripName;
        
        // Requesting user details
        private String requestingUserId;
        private String requestingUserName;
        private String requestingUserEmail;
        private String requestMessage;
        private Instant requestedAt;
        
        // Complete user profile
        private UserProfile requestingUserProfile;
        
        // Voting status
        private boolean hasCurrentUserVoted;
        private String currentUserVote; // "APPROVED", "REJECTED", or null
        private int totalVotesReceived;
        private int totalVotesRequired;
        private List<String> pendingMemberIds; // Who hasn't voted yet
        
        // Urgency indicator
        private String urgencyLevel; // "high", "medium", "low"
        private int daysPending;
    }
    
    @Data
    public static class UserProfile {
        private String userId;
        private String name;
        private String email;
        private String nationality;
        private List<String> languages;
        private String profileImageUrl;
        private boolean profileComplete;
        
        // Travel preferences
        private Integer age;
        private List<String> interests;
        private String travelExperience;
        private String budgetLevel;
        private String activityPacing;
        private List<String> preferredTerrains;
        private List<String> preferredActivities;
    }
}