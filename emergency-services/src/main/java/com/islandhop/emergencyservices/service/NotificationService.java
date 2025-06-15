package com.islandhop.emergencyservices.service;

import com.islandhop.emergencyservices.dto.NotificationRequest;
import com.islandhop.emergencyservices.entity.NotificationLog;

public interface NotificationService {
    NotificationLog sendNotification(NotificationRequest request);
    void sendEmergencyAlert(Long alertId, String message);
} 