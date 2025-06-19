package com.islandhop.emergencyservices.service;

import com.islandhop.emergencyservices.dto.NotificationRequest;
import com.islandhop.emergencyservices.model.NotificationLog;
import com.islandhop.emergencyservices.model.EmergencyAlert;

public interface NotificationService {
    NotificationLog sendNotification(NotificationRequest request);
    void sendEmergencyAlert(Long alertId, String message);
    void sendEmergencyNotifications(EmergencyAlert alert);
} 