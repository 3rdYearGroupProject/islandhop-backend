package com.islandhop.emergencyservices.config;

import com.islandhop.emergencyservices.model.EmergencyAlert;
import com.islandhop.emergencyservices.model.NotificationLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, EmergencyAlert> emergencyAlertRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, EmergencyAlert> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(EmergencyAlert.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, NotificationLog> notificationLogRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, NotificationLog> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(NotificationLog.class));
        return template;
    }
} 