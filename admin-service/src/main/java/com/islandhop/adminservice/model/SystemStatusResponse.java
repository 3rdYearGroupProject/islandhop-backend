package com.islandhop.adminservice.model;

/**
 * Response model for system status endpoint.
 * Contains the status of external services (Redis, Firebase, MongoDB).
 */
public class SystemStatusResponse {

    private String redis;
    private String firebase;
    private String mongodb;

    /**
     * Default constructor
     */
    public SystemStatusResponse() {}

    /**
     * Constructor with all parameters
     */
    public SystemStatusResponse(String redis, String firebase, String mongodb) {
        this.redis = redis;
        this.firebase = firebase;
        this.mongodb = mongodb;
    }

    // Getters and setters
    public String getRedis() {
        return redis;
    }

    public void setRedis(String redis) {
        this.redis = redis;
    }

    public String getFirebase() {
        return firebase;
    }

    public void setFirebase(String firebase) {
        this.firebase = firebase;
    }

    public String getMongodb() {
        return mongodb;
    }

    public void setMongodb(String mongodb) {
        this.mongodb = mongodb;
    }

    /**
     * Service status constants
     */
    public static final class Status {
        public static final String UP = "UP";
        public static final String DOWN = "DOWN";
    }
}
