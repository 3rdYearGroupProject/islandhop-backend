package com.islandhop.userservices.service;

import com.islandhop.userservices.dto.UserAccountResponse;
import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * Validates the Firebase ID token and returns user details (role, email, etc.) if valid.
     * Returns null if invalid or not found.
     */
    Map<String, Object> validateAndGetUserDetails(String idToken);

    /**
     * Get all users in the system
     * @return List of user account responses containing user details
     */
    List<UserAccountResponse> getAllUsers();

    /**
     * Update user account status
     * @param email User email to update
     * @param status New status to set
     * @throws IllegalArgumentException if user not found or invalid status
     */
    void updateUserStatus(String email, String status);
}