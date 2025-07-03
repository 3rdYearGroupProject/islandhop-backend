package com.islandhop.emergencyservices.service.impl;

import com.islandhop.emergencyservices.dto.EmergencyAlertRequest;
import com.islandhop.emergencyservices.dto.EmergencyAlertResponse;
import com.islandhop.emergencyservices.exception.ResourceNotFoundException;
import com.islandhop.emergencyservices.model.AlertStatus;
import com.islandhop.emergencyservices.model.EmergencyAlert;
import com.islandhop.emergencyservices.repository.EmergencyAlertRepository;
import com.islandhop.emergencyservices.service.EmergencyAlertService;
import com.islandhop.emergencyservices.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyAlertServiceImpl implements EmergencyAlertService {

    private final EmergencyAlertRepository alertRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public EmergencyAlertResponse createAlert(EmergencyAlertRequest request) {
        EmergencyAlert alert = new EmergencyAlert();
        alert.setUserId(request.getUserId());
        alert.setBookingId(request.getBookingId());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setTriggeredBy(request.getTriggeredBy());

        EmergencyAlert savedAlert = alertRepository.save(alert);
        notificationService.sendEmergencyNotifications(savedAlert);
        
        return mapToResponse(savedAlert);
    }

    @Override
    public EmergencyAlertResponse getAlertById(Long id) {
        EmergencyAlert alert = alertRepository.findById(id);
        if (alert == null) {
            throw new ResourceNotFoundException("Alert not found with id: " + id);
        }
        return mapToResponse(alert);
    }

    @Override
    public List<EmergencyAlertResponse> getAlertsByUserId(String userId) {
        return alertRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmergencyAlertResponse> getAlertsByBookingId(String bookingId) {
        return alertRepository.findByBookingId(bookingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmergencyAlertResponse updateAlertStatus(Long id, AlertStatus status) {
        EmergencyAlert alert = alertRepository.findById(id);
        if (alert == null) {
            throw new ResourceNotFoundException("Alert not found with id: " + id);
        }
        alert.setStatus(status);
        return mapToResponse(alertRepository.save(alert));
    }

    @Override
    public List<EmergencyAlertResponse> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EmergencyAlertResponse mapToResponse(EmergencyAlert alert) {
        EmergencyAlertResponse response = new EmergencyAlertResponse();
        response.setId(alert.getId());
        response.setUserId(alert.getUserId());
        response.setBookingId(alert.getBookingId());
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setTimestamp(alert.getTimestamp());
        response.setStatus(alert.getStatus());
        response.setTriggeredBy(alert.getTriggeredBy());
        return response;
    }
} 