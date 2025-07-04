package com.islandhop.tripplanning.dto;

import com.islandhop.tripplanning.model.PlannedPlace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Enhanced request for adding places to a specific day with contextual information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPlaceToDayRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty
    private String placeName;
    
    private String city;
    private String description;
    private Double latitude;
    private Double longitude;
    
    @NotNull
    @Min(value = 1, message = "Day number must be at least 1")
    private Integer dayNumber;
    
    @NotNull
    private PlannedPlace.PlaceType placeType; // HOTEL, ATTRACTION, RESTAURANT, etc.
    
    private Integer estimatedVisitDurationMinutes;
    private String preferredTimeSlot; // "morning", "afternoon", "evening"
    
    // Context for better recommendations
    private String previousPlaceId; // ID of the place they added before this one
    private Boolean isAccommodation = false; // If true, this is where they're staying
    private Integer priority = 5; // 1-10, how important this place is to them
}
