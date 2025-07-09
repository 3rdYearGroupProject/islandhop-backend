package com.islandhop.trip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.islandhop.trip.model.DailyPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for retrieving full trip plan information.
 * Contains complete trip details including all daily plans.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripPlanResponse {
    
    private String status = "success";
    private String tripId;
    private String userId;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private List<DailyPlan> dailyPlans;
    private String message;
    
    /**
     * Constructor for successful response
     */
    public TripPlanResponse(String tripId, String userId, String destination, 
                           LocalDate startDate, LocalDate endDate, 
                           int numberOfDays, List<DailyPlan> dailyPlans) {
        this.status = "success";
        this.tripId = tripId;
        this.userId = userId;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.dailyPlans = dailyPlans;
        this.message = "Trip plan retrieved successfully";
    }
    
    /**
     * Constructor for error response
     */
    public TripPlanResponse(String status, String tripId, String message) {
        this.status = status;
        this.tripId = tripId;
        this.message = message;
    }
}
