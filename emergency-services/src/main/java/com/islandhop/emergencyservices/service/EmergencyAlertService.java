package com.islandhop.emergencyservices.service;

import com.islandhop.emergencyservices.dto.EmergencyAlertRequest;
import com.islandhop.emergencyservices.dto.EmergencyAlertResponse;
import com.islandhop.emergencyservices.model.AlertStatus;
import java.util.List;

public interface EmergencyAlertService {
    EmergencyAlertResponse createAlert(EmergencyAlertRequest request);
    EmergencyAlertResponse getAlertById(Long id);
    List<EmergencyAlertResponse> getAlertsByUserId(String userId);
    List<EmergencyAlertResponse> getAlertsByBookingId(String bookingId);
    EmergencyAlertResponse updateAlertStatus(Long id, AlertStatus status);
    List<EmergencyAlertResponse> getAlertsByStatus(AlertStatus status);
} 