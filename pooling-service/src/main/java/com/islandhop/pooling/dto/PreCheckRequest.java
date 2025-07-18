package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for pre-checking compatible public groups.
 * Used before creating a new group to show existing options.
 */
@Data
public class PreCheckRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Base city is required")
    private String baseCity;
    
    @NotBlank(message = "Start date is required")
    private String startDate; // YYYY-MM-DD format
    
    @NotBlank(message = "End date is required")
    private String endDate; // YYYY-MM-DD format
    
    @NotBlank(message = "Budget level is required")
    private String budgetLevel; // Low, Medium, High
    
    private List<String> preferredActivities;
    
    private List<String> preferredTerrains;
    
    private String activityPacing; // Relaxed, Normal, Fast
    
    @NotNull(message = "Multi-city preference is required")
    private Boolean multiCityAllowed;
}
