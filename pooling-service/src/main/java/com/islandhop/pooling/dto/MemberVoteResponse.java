package com.islandhop.pooling.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for member vote on join request.
 * Provides information about the voting status.
 */
@Data
public class MemberVoteResponse {
    
    private String status;
    
    private String groupId;
    
    private String joinRequestId;
    
    private String message;
    
    private String requestStatus; // "pending", "approved", "rejected"
    
    private List<String> pendingMembers; // Members who haven't voted yet
    
    private int totalVotesReceived;
    
    private int totalMembersRequired;
}
