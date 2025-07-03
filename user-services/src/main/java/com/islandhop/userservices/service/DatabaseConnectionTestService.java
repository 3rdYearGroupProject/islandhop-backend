package com.islandhop.userservices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class DatabaseConnectionTestService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTestService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void testDatabaseConnections() {
        testPostgreSQLConnection();
        testRedisConnection();
    }

    private void testPostgreSQLConnection() {
        try {
            Connection connection = dataSource.getConnection();
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();
            String url = connection.getMetaData().getURL();
            connection.close();
            
            logger.info("✅ PostgreSQL connection successful!");
            logger.info("📊 Database: {} {}", databaseProductName, databaseProductVersion);
            logger.info("🔗 URL: {}", url);
            
        } catch (Exception e) {
            logger.error("❌ PostgreSQL connection failed: {}", e.getMessage());
        }
    }

    private void testRedisConnection() {
        try {
            // Test Redis connection by setting and getting a value
            String testKey = "connection:test";
            String testValue = "success";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            if (testValue.equals(retrievedValue)) {
                logger.info("✅ Redis connection successful!");
                logger.info("🔑 Test key-value operation completed successfully");
                
                // Clean up test data
                redisTemplate.delete(testKey);
            } else {
                logger.warn("⚠️ Redis connection established but data integrity issue detected");
            }
            
        } catch (Exception e) {
            logger.error("❌ Redis connection failed: {}", e.getMessage());
        }
    }
}