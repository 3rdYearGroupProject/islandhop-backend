package com.islandhop.pooling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedActivity {
    
    private String activityId;
    private String activityName;
    private String activityType; // ATTRACTION, RESTAURANT, TRANSPORT, ACCOMMODATION
    private String location;
    private double latitude;
    private double longitude;
    
    // Timing
    private LocalDate date;
    private LocalTime suggestedTime;
    private Integer estimatedDurationMinutes;
    
    // Participation
    private Integer participantCount;
    private boolean isOptional;
    private String organizerId; // User who suggested this activity
    
    // Details
    private String description;
    private String meetingPoint;
    private Double estimatedCostPerPerson;
    private String difficultyLevel; // EASY, MODERATE, CHALLENGING
}
