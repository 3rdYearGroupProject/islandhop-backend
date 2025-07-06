package com.islandhop.adminservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for system status endpoint.
 * Contains the status of external services (Redis, Firebase, MongoDB).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusResponse {

    private String redis;
    private String firebase;
    private String mongodb;

    /**
     * Service status constants
     */
    public static final class Status {
        public static final String UP = "UP";
        public static final String DOWN = "DOWN";
    }
}
