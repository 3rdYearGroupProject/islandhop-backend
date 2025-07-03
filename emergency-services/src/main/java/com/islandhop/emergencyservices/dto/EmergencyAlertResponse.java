package com.islandhop.emergencyservices.dto;

import com.islandhop.emergencyservices.model.AlertStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmergencyAlertResponse {
    private Long id;
    private String userId;
    private String bookingId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private AlertStatus status;
    private String triggeredBy;
} 