package com.islandhop.tripinit;

import com.islandhop.tripinit.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for TripInitiationApplication.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
@ActiveProfiles("test")
class TripInitiationApplicationTests {

    @Test
    void contextLoads() {
        // Simple test to ensure Spring context loads
    }
}