package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannedActivity {
    
    private String activityId;
    private PlannedPlace place;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private ActivityType type;
    private String notes;
    private boolean confirmed;
    
    public enum ActivityType {
        VISIT, MEAL, TRAVEL, REST, SHOPPING, CHECK_IN, CHECK_OUT
    }
}
