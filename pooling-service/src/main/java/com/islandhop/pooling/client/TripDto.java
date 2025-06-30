package com.islandhop.pooling.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

// DTOs for Trip Planning Service Integration

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {
    private String tripId;
    private String userId;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime arrivalTime;
    private String baseCity;
    private boolean multiCity;
    private List<String> categories;
    private String pacing; // RELAXED, NORMAL, ACTIVE
    private List<PlannedPlaceDto> places;
    private List<DayPlanDto> dayPlans;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private Map<String, Object> preferences;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlannedPlaceDto {
    private String placeId;
    private String name;
    private String city;
    private double latitude;
    private double longitude;
    private String description;
    private List<String> categories;
    private String type;
    private Integer estimatedVisitDurationMinutes;
    private LocalTime suggestedArrivalTime;
    private LocalTime suggestedDepartureTime;
    private Integer dayNumber;
    private Integer orderInDay;
    private String address;
    private Double rating;
    private String priceLevel;
    private boolean userAdded;
    private boolean confirmed;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DayPlanDto {
    private Integer dayNumber;
    private LocalDate date;
    private String baseCity;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalTravelTimeMinutes;
    private Integer totalVisitTimeMinutes;
    private Integer totalActivities;
    private String pacingAssessment;
}
