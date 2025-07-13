package com.islandhop.tripinit.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for trip initiation endpoint.
 * Contains calculated costs, distance, and route summary.
 */
@Data
@Builder
public class TripInitiationResponse {
    private String tripId;
    private String userId;
    private Double averageTripDistance;
    private Double averageDriverCost;
    private Double averageGuideCost;
    private String vehicleType;
    private List<RouteSummary> routeSummary;
}