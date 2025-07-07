package com.islandhop.userservices.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void testConnection() {
        try {
            redisTemplate.opsForValue().set("test:connection", "success");
            String result = (String) redisTemplate.opsForValue().get("test:connection");
            System.out.println("Redis connection test: " + result);
        } catch (Exception e) {
            System.out.println("Redis connection failed: " + e.getMessage());
        }
    }
}