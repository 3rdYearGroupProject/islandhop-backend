package com.islandhop.pooling.dto;

import lombok.Data;
import java.util.List;

/**
 * Response DTO for finalizing a trip with suggestions support.
 * Enhanced to include compatibility suggestions for hybrid workflow.
 */
@Data
public class FinalizeTripResponse {
    
    private String status;
    private String message;
    private String groupId;
    private String tripId;
    private String action; // "finalized", "joined", or "suggestions"
    private boolean success;
    
    // For suggestions action
    private List<TripSuggestionsResponse.CompatibleGroup> suggestions;
}
