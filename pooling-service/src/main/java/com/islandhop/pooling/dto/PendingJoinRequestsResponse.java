package com.islandhop.pooling.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for getting pending join requests that require member votes.
 */
@Data
public class PendingJoinRequestsResponse {
    
    private String status;
    
    private List<PendingJoinRequestInfo> pendingRequests;
    
    @Data
    public static class PendingJoinRequestInfo {
        private String joinRequestId;
        private String userId;
        private String userName;
        private String userEmail;
        private String message;
        private String requestedAt;
        private List<String> pendingMembers;
        private int totalVotesReceived;
        private int totalMembersRequired;
        private boolean hasCurrentUserVoted;
    }
}
