package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for voting on a join request using user ID path parameter.
 * Used with the new voting endpoint: /{groupId}/join-requests/{requestUserId}/vote
 */
@Data
public class JoinRequestVoteRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId; // The ID of the member casting the vote
    
    private boolean approved; // true for approve, false for reject
    
    private String comment; // Optional comment explaining the vote decision
}
