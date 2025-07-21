package com.islandhop.tripinit.service;

import com.islandhop.tripinit.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for TripInitiationService.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
@ActiveProfiles("test")
public class TripInitiationServiceTest {

    @Test
    void contextLoads() {
        // Simple test to ensure Spring context loads
    }
}