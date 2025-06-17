package com.islandhop.emergencyservices.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmergencyAlert {
    private Long id;
    private String userId;
    private String bookingId;
    private String description;
    private AlertStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String location;
    private String contactNumber;
    private Double latitude;
    private Double longitude;
    private String triggeredBy;
    private LocalDateTime timestamp;
}
