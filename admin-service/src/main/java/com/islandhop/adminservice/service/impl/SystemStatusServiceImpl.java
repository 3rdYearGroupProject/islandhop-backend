package com.islandhop.adminservice.service.impl;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.islandhop.adminservice.model.SystemStatusResponse;
import com.islandhop.adminservice.service.SystemStatusService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.bson.Document;


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

    // Add this method to SystemStatusServiceImpl.java

@Override
public String getRedisStatus() {
    try {
        // Simple ping operation with timeout
        String result = redisTemplate.execute((RedisCallback<String>) connection -> {
            return connection.ping();
        });
        
        if ("PONG".equals(result)) {
            logger.debug("Redis connection successful");
            return SystemStatusResponse.Status.UP;
        } else {
            logger.warn("Redis ping returned unexpected response: {}", result);
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
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection("ping_check").document("test").get().get(); // blocking call
        logger.debug("Firebase Firestore connection successful");
        return SystemStatusResponse.Status.UP;
    } catch (Exception e) {
        logger.error("Firebase connection failed: {}", e.getMessage());
        return SystemStatusResponse.Status.DOWN;
    }
}


    @Override
public String getMongoDbStatus() {
    try {
        Document ping = new Document("ping", 1);
        mongoTemplate.getDb().runCommand(ping); // sends real ping to DB
        logger.debug("MongoDB connection successful (ping)");
        return SystemStatusResponse.Status.UP;
    } catch (Exception e) {
        logger.error("MongoDB ping failed: {}", e.getMessage());
        return SystemStatusResponse.Status.DOWN;
    }
}
   
}
