package com.islandhop.emergencyservices.controller;

import com.islandhop.emergencyservices.dto.EmergencyAlertRequest;
import com.islandhop.emergencyservices.dto.EmergencyAlertResponse;
import com.islandhop.emergencyservices.model.AlertStatus;
import com.islandhop.emergencyservices.service.EmergencyAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emergency")
@RequiredArgsConstructor
public class EmergencyAlertController {

    private final EmergencyAlertService emergencyAlertService;

    @PostMapping("/trigger")
    public ResponseEntity<EmergencyAlertResponse> triggerEmergencyAlert(
            @Valid @RequestBody EmergencyAlertRequest request) {
        return ResponseEntity.ok(emergencyAlertService.createAlert(request));
    }

    @GetMapping("/status/{alertId}")
    public ResponseEntity<EmergencyAlertResponse> getAlertStatus(
            @PathVariable Long alertId) {
        return ResponseEntity.ok(emergencyAlertService.getAlertById(alertId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmergencyAlertResponse>> getAlertsByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(emergencyAlertService.getAlertsByUserId(userId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<EmergencyAlertResponse>> getAlertsByBooking(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(emergencyAlertService.getAlertsByBookingId(bookingId));
    }

    @PutMapping("/resolve/{alertId}")
    public ResponseEntity<EmergencyAlertResponse> resolveAlert(
            @PathVariable Long alertId) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(alertId, AlertStatus.RESOLVED));
    }

    @GetMapping("/status/list/{status}")
    public ResponseEntity<List<EmergencyAlertResponse>> getAlertsByStatus(
            @PathVariable AlertStatus status) {
        return ResponseEntity.ok(emergencyAlertService.getAlertsByStatus(status));
    }
} 