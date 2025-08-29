package com.islandhop.pooling.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for user invitations.
 * Contains all pending invitations for a specific user.
 */
@Data
public class UserInvitationsResponse {
    
    private List<InvitationDetail> invitations;
    
    private int totalInvitations;
}
