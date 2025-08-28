package com.islandhop.pooling.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for getting all pending join requests across all groups where a user is a member.
 * Provides a consolidated view of all requests requiring the user's attention.
 */
@Data
public class AllPendingRequestsResponse {
    
    private String status;
    private String message;
    private int totalGroups;
    private int totalPendingRequests;
    private List<GroupWithPendingRequests> groups;
    
    @Data
    public static class GroupWithPendingRequests {
        private String groupId;
        private String groupName;
        private String tripName;
        private String visibility; // "public" or "private"
        private int currentMembers;
        private int maxMembers;
        private int pendingRequestsCount;
        private List<PendingJoinRequestInfo> pendingRequests;
    }
    
    @Data
    public static class PendingJoinRequestInfo {
        private String joinRequestId;
        private String userId;
        private String userName;
        private String userEmail;
        private String message;
        private String requestedAt;
        private UserProfileSummary userProfile;
        
        // Voting information
        private List<String> pendingMembers; // Members who haven't voted yet
        private int totalVotesReceived;
        private int totalMembersRequired;
        private boolean hasCurrentUserVoted;
        private String urgencyLevel; // "high", "medium", "low" based on how long request has been pending
    }
    
    @Data
    public static class UserProfileSummary {
        private Integer age;
        private String nationality;
        private List<String> interests;
        private String travelExperience;
        private String budgetLevel;
        private String activityPacing;
        private List<String> preferredTerrains;
        private List<String> preferredActivities;
    }
}
