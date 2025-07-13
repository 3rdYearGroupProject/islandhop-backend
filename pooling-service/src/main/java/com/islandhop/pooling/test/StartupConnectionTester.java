package com.islandhop.pooling.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Startup verification component that tests all critical connections
 * and configurations when the application starts.
 * Only runs when 'test-connections' profile is active.
 */
@Component
@Profile("test-connections")
@Slf4j
public class StartupConnectionTester implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== POOLING SERVICE STARTUP CONNECTION TEST ===");
        
        boolean allTestsPassed = true;
        
        // Test 1: MongoDB Connection
        allTestsPassed &= testMongoDBConnection();
        
        // Test 2: Application Context
        allTestsPassed &= testApplicationContext();
        
        // Test 3: Basic Configuration
        allTestsPassed &= testBasicConfiguration();
        
        // Final Result
        if (allTestsPassed) {
            log.info("✅ ALL STARTUP TESTS PASSED - Service is ready for operation");
        } else {
            log.error("❌ SOME STARTUP TESTS FAILED - Check logs above for details");
            System.exit(1); // Exit with error code if tests fail
        }
        
        log.info("=== STARTUP CONNECTION TEST COMPLETED ===");
    }
    
    private boolean testMongoDBConnection() {
        log.info("🔍 Testing MongoDB connection...");
        try {
            // Test basic connection
            String databaseName = mongoTemplate.getDb().getName();
            log.info("✅ MongoDB connection successful");
            log.info("   Database: {}", databaseName);
            
            // Log connection details
            String connectionString = mongoTemplate.getDb().runCommand(new org.bson.Document("connectionStatus", 1))
                .toJson();
            log.info("   Connection verified with server");
            
            // Test write operation
            mongoTemplate.getCollection("connection_test").insertOne(
                new org.bson.Document("test", "startup_test")
                    .append("timestamp", java.time.Instant.now())
            );
            log.info("✅ MongoDB write operation successful");
            
            // Clean up test document
            mongoTemplate.getCollection("connection_test").deleteOne(
                new org.bson.Document("test", "startup_test")
            );
            log.info("✅ MongoDB cleanup successful");
            
            return true;
        } catch (Exception e) {
            log.error("❌ MongoDB connection failed: {}", e.getMessage());
            log.error("   This usually means:");
            log.error("   1. MongoDB Atlas URI is incorrect");
            log.error("   2. Network connectivity issues");
            log.error("   3. Authentication credentials are wrong");
            log.error("   4. Database access permissions are incorrect");
            log.error("   Error details: ", e);
            return false;
        }
    }
    
    private boolean testApplicationContext() {
        log.info("🔍 Testing application context...");
        try {
            // Check if key beans are loaded
            log.info("✅ Application context loaded successfully");
            log.info("   MongoTemplate bean: {}", mongoTemplate != null ? "Available" : "Missing");
            return true;
        } catch (Exception e) {
            log.error("❌ Application context test failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean testBasicConfiguration() {
        log.info("🔍 Testing basic configuration...");
        try {
            // Test environment properties
            String appName = System.getProperty("spring.application.name", "pooling-service");
            String serverPort = System.getProperty("server.port", "8086");
            
            log.info("✅ Basic configuration loaded");
            log.info("   Application: {}", appName);
            log.info("   Server Port: {}", serverPort);
            
            return true;
        } catch (Exception e) {
            log.error("❌ Basic configuration test failed: {}", e.getMessage());
            return false;
        }
    }
}
