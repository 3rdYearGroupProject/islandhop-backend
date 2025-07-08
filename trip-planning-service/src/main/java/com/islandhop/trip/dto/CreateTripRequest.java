package com.islandhop.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating a new trip itinerary.
 * Contains all user-provided information needed to initialize a trip plan.
 */
@Data
public class CreateTripRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Trip name is required")
    private String tripName;

    @NotBlank(message = "Start date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in YYYY-MM-DD format")
    private String startDate;

    @NotBlank(message = "End date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in YYYY-MM-DD format")
    private String endDate;

    @Pattern(regexp = "^$|^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Arrival time must be in HH:mm format or empty")
    private String arrivalTime = "";

    @NotBlank(message = "Base city is required")
    private String baseCity;

    private Boolean multiCityAllowed = true;

    @Pattern(regexp = "^(Relaxed|Normal|Fast)$", message = "Activity pacing must be one of: Relaxed, Normal, Fast")
    private String activityPacing = "Normal";

    @Pattern(regexp = "^(Low|Medium|High)$", message = "Budget level must be one of: Low, Medium, High")
    private String budgetLevel = "Medium";

    private List<String> preferredTerrains = List.of();

    private List<String> preferredActivities = List.of();
}
