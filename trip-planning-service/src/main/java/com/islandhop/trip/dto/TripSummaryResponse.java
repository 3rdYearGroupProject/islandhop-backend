package com.islandhop.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for trip summary information.
 * Used for listing trips without detailed daily plans.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSummaryResponse {
    
    private String status;
    private String tripId;
    private String userId;
    private String tripName;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private String message;

    /**
     * Constructor for error responses.
     */
    public TripSummaryResponse(String status, String tripId, String message) {
        this.status = status;
        this.tripId = tripId;
        this.message = message;
    }
}
