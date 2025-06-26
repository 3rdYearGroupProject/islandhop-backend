package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannedPlace {
    
    private String placeId;
    private String name;
    private String city;
    private double latitude;
    private double longitude;
    private String description;
    private List<String> categories;
    private PlaceType type; // ATTRACTION, HOTEL, RESTAURANT, TRANSPORT_HUB
    
    // Visit planning details
    private Integer estimatedVisitDurationMinutes;
    private LocalTime suggestedArrivalTime;
    private LocalTime suggestedDepartureTime;
    private Integer dayNumber; // Which day of the trip
    private Integer orderInDay; // Order within the day
    
    // Additional metadata
    private String address;
    private String phoneNumber;
    private String website;
    private List<String> openingHours;
    private Double rating;
    private Integer reviewCount;
    private String priceLevel; // FREE, BUDGET, MODERATE, EXPENSIVE
    
    // User interaction
    private boolean userAdded; // true if manually added by user
    private boolean confirmed; // true if user confirmed this suggestion
    
    public enum PlaceType {
        ATTRACTION, HOTEL, RESTAURANT, TRANSPORT_HUB, VIEWPOINT, SHOPPING
    }
}
