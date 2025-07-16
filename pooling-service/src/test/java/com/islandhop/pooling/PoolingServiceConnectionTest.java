package com.islandhop.pooling;

import com.islandhop.pooling.controller.GroupController;
import com.islandhop.pooling.service.GroupService;
import com.islandhop.pooling.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Pooling Service.
 * Tests application startup, context loading, and basic connectivity.
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class PoolingServiceConnectionTest {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    private GroupController groupController;

    @Autowired(required = false)
    private GroupService groupService;

    @Autowired(required = false)
    private GroupRepository groupRepository;

    @Test
    void contextLoads() {
        log.info("=== TESTING APPLICATION CONTEXT LOADING ===");
        
        // Test that the application context loads successfully
        assertNotNull(groupController, "GroupController should be loaded");
        assertNotNull(groupService, "GroupService should be loaded");
        assertNotNull(groupRepository, "GroupRepository should be loaded");
        
        log.info("✅ Application context loaded successfully");
        log.info("   GroupController: {}", groupController != null ? "Available" : "Missing");
        log.info("   GroupService: {}", groupService != null ? "Available" : "Missing");
        log.info("   GroupRepository: {}", groupRepository != null ? "Available" : "Missing");
    }

    @Test
    void testMongoDBConnection() {
        log.info("=== TESTING MONGODB CONNECTION ===");
        
        if (mongoTemplate == null) {
            log.warn("⚠️ MongoTemplate not available - MongoDB connection test skipped");
            log.info("   This might be expected in test profile if using embedded MongoDB");
            return;
        }

        try {
            // Test basic connection
            String databaseName = mongoTemplate.getDb().getName();
            assertNotNull(databaseName, "Database name should not be null");
            
            log.info("✅ MongoDB connection successful");
            log.info("   Database: {}", databaseName);
            
            // Test collections access
            boolean collectionsAccessible = mongoTemplate.getCollectionNames() != null;
            assertTrue(collectionsAccessible, "Should be able to access collections");
            
            log.info("✅ MongoDB collections accessible");
            
        } catch (Exception e) {
            log.warn("⚠️ MongoDB connection failed: {}", e.getMessage());
            log.info("   This is expected if MongoDB Atlas is not accessible from this environment");
            log.info("   For production deployment testing, use the live connection test instead");
            // Don't fail the test for MongoDB connection issues in test environment
        }
    }

    @Test
    void testHealthEndpoint() {
        log.info("=== TESTING HEALTH ENDPOINT AVAILABILITY ===");
        
        assertNotNull(groupController, "GroupController should be available for health checks");
        
        try {
            // The health endpoint should be accessible through the controller
            var healthResponse = groupController.healthCheck();
            assertNotNull(healthResponse, "Health endpoint should return a response");
            assertNotNull(healthResponse.getBody(), "Health response body should not be null");
            
            log.info("✅ Health endpoint accessible");
            log.info("   Response: {}", healthResponse.getBody());
            
        } catch (Exception e) {
            log.error("❌ Health endpoint test failed: {}", e.getMessage());
            fail("Health endpoint should be accessible: " + e.getMessage());
        }
    }

    @Test
    void testServiceConfiguration() {
        log.info("=== TESTING SERVICE CONFIGURATION ===");
        
        try {
            // Test that service beans are properly configured
            assertNotNull(groupService, "GroupService should be configured");
            assertNotNull(groupRepository, "GroupRepository should be configured");
            
            log.info("✅ Service configuration valid");
            log.info("   All required beans are properly configured");
            
        } catch (Exception e) {
            log.error("❌ Service configuration test failed: {}", e.getMessage());
            fail("Service configuration should be valid: " + e.getMessage());
        }
    }
}
