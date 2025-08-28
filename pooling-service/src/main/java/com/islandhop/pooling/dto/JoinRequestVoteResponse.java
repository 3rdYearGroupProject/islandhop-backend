package com.islandhop.pooling.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for voting on a join request using user ID path parameter.
 * Returns comprehensive information about the vote and current request status.
 */
@Data
public class JoinRequestVoteResponse {
    
    private String voterUserId; // ID of the member who cast the vote
    private String requestUserId; // ID of the user who made the join request
    private String groupId; // ID of the group
    private String voteDecision; // "approved" or "rejected"
    private String comment; // Comment provided with the vote
    private Instant votedAt; // When the vote was cast
    
    // Request status after this vote
    private String requestStatus; // "pending", "approved", "rejected"
    private String message; // Human-readable status message
    
    // Voting progress information
    private int totalVotesReceived; // How many members have voted so far
    private int totalMembersRequired; // Total number of members required to vote
    private List<String> pendingMemberIds; // List of member IDs who haven't voted yet
}
