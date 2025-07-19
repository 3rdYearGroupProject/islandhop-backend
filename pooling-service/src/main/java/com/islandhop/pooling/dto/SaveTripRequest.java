package com.islandhop.pooling.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class SaveTripRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Trip ID is required")
    private String tripId;
    
    @NotNull(message = "Trip data is required")
    private TripData tripData;
    
    @Data
    public static class TripData {
        private String name;
        private String startDate;
        private String endDate;
        private List<Destination> destinations;
        private List<String> terrains;
        private List<String> activities;
        private Map<String, DayItinerary> itinerary;
        
        @Data
        public static class Destination {
            private String name;
        }
        
        @Data
        public static class DayItinerary {
            private String date;
            private List<Object> activities;
            private List<Object> places;
            private List<Object> food;
            private List<Object> transportation;
        }
    }
}