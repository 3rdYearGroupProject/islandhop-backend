package com.islandhop.tripplanning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private List<String> terrainPreferences;
    private List<String> activityPreferences;
}
