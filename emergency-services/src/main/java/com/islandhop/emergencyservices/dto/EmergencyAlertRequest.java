package com.islandhop.emergencyservices.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmergencyAlertRequest {
    @NotNull(message = "User ID is required")
    private String userId;

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Triggered by information is required")
    private String triggeredBy;
} 