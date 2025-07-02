package com.islandhop.pooling.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayPlanDto {
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
