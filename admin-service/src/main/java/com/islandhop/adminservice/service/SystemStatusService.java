package com.islandhop.adminservice.service;

import com.islandhop.adminservice.model.SystemStatusResponse;

/**
 * Service interface for system status monitoring.
 * Provides methods to check the health of external services.
 */
public interface SystemStatusService {

    /**
     * Gets the overall system status by checking all external services.
     * 
     * @return SystemStatusResponse containing the status of all services
     */
    SystemStatusResponse getSystemStatus();

    /**
     * Checks Redis connection status.
     * 
     * @return "UP" if Redis is accessible, "DOWN" otherwise
     */
    String getRedisStatus();

    /**
     * Checks Firebase connection status.
     * 
     * @return "UP" if Firebase is accessible, "DOWN" otherwise
     */
    String getFirebaseStatus();

    /**
     * Checks MongoDB connection status.
     * 
     * @return "UP" if MongoDB is accessible, "DOWN" otherwise
     */
    String getMongoDbStatus();
}
