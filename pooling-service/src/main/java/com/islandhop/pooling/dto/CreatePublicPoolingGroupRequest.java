package com.islandhop.pooling.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new public pooling group.
 * Includes trip planning preferences and group configuration.
 */
@Data
public class CreatePublicPoolingGroupRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Group name is required")
    private String groupName;
    
    @NotBlank(message = "Trip name is required")
    private String tripName;
    
    @NotBlank(message = "Start date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in YYYY-MM-DD format")
    private String startDate;
    
    @NotBlank(message = "End date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in YYYY-MM-DD format")
    private String endDate;
    
    @NotBlank(message = "Base city is required")
    private String baseCity;
    
    @Pattern(regexp = "^$|^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Arrival time must be in HH:mm format or empty")
    private String arrivalTime = "";
    
    private Boolean multiCityAllowed = true;
    
    @Pattern(regexp = "^(Relaxed|Normal|Fast)$", message = "Activity pacing must be one of: Relaxed, Normal, Fast")
    private String activityPacing = "Normal";
    
    @Pattern(regexp = "^(Low|Medium|High)$", message = "Budget level must be one of: Low, Medium, High")
    private String budgetLevel = "Medium";
    
    private List<String> preferredTerrains = List.of();
    
    private List<String> preferredActivities = List.of();
    
    @Min(value = 2, message = "Maximum members must be at least 2")
    @Max(value = 20, message = "Maximum members cannot exceed 20")
    private Integer maxMembers = 6; // Default group size
    
    private Boolean requiresApproval = false; // Default to open joining for public groups
    
    private Map<String, Object> additionalPreferences; // For future extensibility
}
