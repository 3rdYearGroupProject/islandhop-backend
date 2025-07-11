package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for trip suggestions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSuggestionResponse {
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private String destination;
    
    private String startDate;
    
    private String endDate;
    
    private Map<String, Object> preferences;
    
    private Integer score;
    
    private String message;
}
