package com.islandhop.emergencyservices.repository;

import com.islandhop.emergencyservices.model.NotificationLog;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NotificationLogRepository {
    private final RedisTemplate<String, NotificationLog> redisTemplate;
    private static final String KEY_PREFIX = "notification_log:";

    public NotificationLogRepository(RedisTemplate<String, NotificationLog> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public NotificationLog save(NotificationLog log) {
        String key = KEY_PREFIX + log.getId();
        redisTemplate.opsForValue().set(key, log);
        return log;
    }

    public NotificationLog findById(Long id) {
        String key = KEY_PREFIX + id;
        return redisTemplate.opsForValue().get(key);
    }

    public List<NotificationLog> findByAlertId(Long alertId) {
        return redisTemplate.keys(KEY_PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(log -> log != null && log.getAlertId().equals(alertId))
                .collect(Collectors.toList());
    }

    public List<NotificationLog> findByRecipientId(String recipientId) {
        return redisTemplate.keys(KEY_PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(log -> log != null && log.getRecipientId().equals(recipientId))
                .collect(Collectors.toList());
    }

    public List<NotificationLog> findByAlertIdAndRead(Long alertId, boolean read) {
        return redisTemplate.keys(KEY_PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(log -> log != null && log.getAlertId().equals(alertId) && log.isRead() == read)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        String key = KEY_PREFIX + id;
        redisTemplate.delete(key);
    }
} 