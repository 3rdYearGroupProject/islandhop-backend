package com.islandhop.emergencyservices.repository;

import com.islandhop.emergencyservices.entity.AlertStatus;
import com.islandhop.emergencyservices.entity.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByUserId(String userId);
    List<EmergencyAlert> findByBookingId(String bookingId);
    List<EmergencyAlert> findByStatus(AlertStatus status);
} 