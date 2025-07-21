package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Response DTO for creating a group with trip planning.
 */
@Data
public class CreateGroupWithTripResponse {
    
    private String status;
    private String groupId;
    private String tripId;
    private String message;
    private boolean isDraft; // Indicates if this is a draft group awaiting suggestions
}
