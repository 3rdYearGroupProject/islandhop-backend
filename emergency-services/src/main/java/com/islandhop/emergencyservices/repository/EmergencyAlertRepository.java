package com.islandhop.emergencyservices.repository;

import com.islandhop.emergencyservices.model.AlertStatus;
import com.islandhop.emergencyservices.model.EmergencyAlert;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class EmergencyAlertRepository {
    private final RedisTemplate<String, EmergencyAlert> redisTemplate;
    private static final String KEY_PREFIX = "emergency_alert:";

    public EmergencyAlertRepository(RedisTemplate<String, EmergencyAlert> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public EmergencyAlert save(EmergencyAlert alert) {
        String key = KEY_PREFIX + alert.getId();
        redisTemplate.opsForValue().set(key, alert);
        return alert;
    }

    public EmergencyAlert findById(Long id) {
        String key = KEY_PREFIX + id;
        return redisTemplate.opsForValue().get(key);
    }

    public List<EmergencyAlert> findByUserId(String userId) {
        return redisTemplate.keys(KEY_PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(alert -> alert != null && alert.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<EmergencyAlert> findByBookingId(String bookingId) {
        return redisTemplate.keys(KEY_PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(alert -> alert != null && alert.getBookingId().equals(bookingId))
                .collect(Collectors.toList());
    }

    public List<EmergencyAlert> findByStatus(AlertStatus status) {
        return redisTemplate.keys(KEY_PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(alert -> alert != null && alert.getStatus() == status)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        String key = KEY_PREFIX + id;
        redisTemplate.delete(key);
    }
}