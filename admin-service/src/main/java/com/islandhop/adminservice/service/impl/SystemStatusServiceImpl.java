package com.islandhop.adminservice.service.impl;

import com.google.firebase.FirebaseApp;
import com.islandhop.adminservice.model.SystemStatusResponse;
import com.islandhop.adminservice.service.SystemStatusService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of SystemStatusService.
 * Provides concrete methods to check the health of external services.
 */
@Service
@RequiredArgsConstructor
public class SystemStatusServiceImpl implements SystemStatusService {

    private static final Logger logger = LoggerFactory.getLogger(SystemStatusServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    @Override
    public SystemStatusResponse getSystemStatus() {
        logger.info("Checking system status for all services...");
        
        String redisStatus = getRedisStatus();
        String firebaseStatus = getFirebaseStatus();
        String mongoDbStatus = getMongoDbStatus();

        logger.info("System status check completed - Redis: {}, Firebase: {}, MongoDB: {}", 
                   redisStatus, firebaseStatus, mongoDbStatus);

        return new SystemStatusResponse(redisStatus, firebaseStatus, mongoDbStatus);
    }

    @Override
    public String getRedisStatus() {
        try {
            // Try to ping Redis
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            
            if ("PONG".equals(pong)) {
                logger.debug("Redis connection successful");
                return SystemStatusResponse.Status.UP;
            } else {
                logger.warn("Redis ping returned unexpected response: {}", pong);
                return SystemStatusResponse.Status.DOWN;
            }
        } catch (Exception e) {
            logger.error("Redis connection failed: {}", e.getMessage());
            return SystemStatusResponse.Status.DOWN;
        }
    }

    @Override
    public String getFirebaseStatus() {
        try {
            // Check if Firebase app is initialized
            FirebaseApp defaultApp = FirebaseApp.getInstance();
            if (defaultApp != null && defaultApp.getName() != null) {
                logger.debug("Firebase connection successful");
                return SystemStatusResponse.Status.UP;
            } else {
                logger.warn("Firebase app not properly initialized");
                return SystemStatusResponse.Status.DOWN;
            }
        } catch (Exception e) {
            logger.error("Firebase connection failed: {}", e.getMessage());
            return SystemStatusResponse.Status.DOWN;
        }
    }

    @Override
    public String getMongoDbStatus() {
        try {
            // Try to execute a simple command to test MongoDB connection
            mongoTemplate.getCollection("test");
            logger.debug("MongoDB connection successful");
            return SystemStatusResponse.Status.UP;
        } catch (Exception e) {
            logger.error("MongoDB connection failed: {}", e.getMessage());
            return SystemStatusResponse.Status.DOWN;
        }
    }
}
