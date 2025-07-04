package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelSegment {
    
    private String fromPlaceId;
    private String toPlaceId;
    private String fromPlaceName;
    private String toPlaceName;
    private double distance; // in kilometers
    private Integer durationMinutes;
    private TravelMode travelMode;
    private String route; // JSON string from Google Maps
    private Double cost; // estimated cost
    
    public enum TravelMode {
        WALKING, DRIVING, PUBLIC_TRANSPORT, TAXI, FLIGHT, TRAIN, BUS
    }
}
