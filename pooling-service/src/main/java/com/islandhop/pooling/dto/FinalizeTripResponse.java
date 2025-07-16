package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Response DTO for finalizing a trip.
 */
@Data
public class FinalizeTripResponse {
    
    private String status;
    private String message;
    private String groupId;
    private String tripId;
    private String action; // "finalized" or "joined"
    private boolean success;
}
