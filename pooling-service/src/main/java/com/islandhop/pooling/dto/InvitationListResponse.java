package com.islandhop.pooling.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for listing user invitations.
 */
@Data
public class InvitationListResponse {
    
    private String status;
    
    private List<InvitationSummary> invitations;
    
    private String message;
    
    @Data
    public static class InvitationSummary {
        private String invitationId;
        private String groupId;
        private String tripId;
        private String tripName;
        private String inviterName;
        private String inviterEmail;
        private String message;
        private Instant invitedAt;
        private Instant expiresAt;
        private String status;
    }
}
