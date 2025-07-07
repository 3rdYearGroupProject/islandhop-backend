package com.islandhop.tripplanning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCitiesRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    // Flexible format for incremental city/day mapping
    private Map<Integer, List<String>> dayToCityMap; // day -> list of cities
    
    // Legacy format support (will be merged incrementally)
    private List<String> cities;
    private Map<String, Integer> cityDays; // city -> number of days
}
