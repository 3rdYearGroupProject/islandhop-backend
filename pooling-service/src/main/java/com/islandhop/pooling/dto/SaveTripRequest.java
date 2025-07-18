package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Request DTO for saving a trip.
 */
@Data
public class SaveTripRequest {
    private String tripId;
    private String userId;
    private String tripName;
}
