package com.islandhop.tripplanning.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnhancedTravelInfoResponse {
    private String fromPlace;
    private String toPlace;
    private double distance;
    private int estimatedDurationMinutes;
    private String primaryTravelMode;
    private List<TravelOption> travelOptions;
    private List<String> pointsOfInterestAlongRoute;
    
    @Data
    public static class TravelOption {
        private String travelMode;
        private int durationMinutes;
        private double distance;
        private double cost;
        private String environmentalImpact;
    }
}
