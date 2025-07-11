package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for public group listing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicGroupResponse {
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private String destination;
    
    private String startDate;
    
    private String endDate;
    
    private Map<String, Object> preferences;
    
    private String message;
}
