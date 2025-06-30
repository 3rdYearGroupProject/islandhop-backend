package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolingRequest {
    
    private String userId;
    private String tripId;
    
    // Search criteria
    private LocalDate startDate;
    private LocalDate endDate;
    private String baseCity;
    private List<String> preferredCities;
    private List<String> interests;
    private String activityPacing;
    
    // Pool preferences
    private Integer maxPoolSize;
    private boolean willingToShareTransport;
    private boolean willingToShareAccommodation;
    private List<String> preferredLanguages;
    private List<String> preferredNationalities;
    
    // Flexibility settings
    private Integer dateFlexibilityDays;
    private Double maxDistanceKm;
    private Double minCompatibilityScore;
}
