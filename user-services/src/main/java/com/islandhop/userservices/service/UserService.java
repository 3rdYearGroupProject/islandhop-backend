package com.islandhop.userservices.service;

import java.util.Map;

public interface UserService {
    /**
     * Validates the Firebase ID token and returns user details (role, email, etc.) if valid.
     * Returns null if invalid or not found.
     */
    Map<String, Object> validateAndGetUserDetails(String idToken);
}