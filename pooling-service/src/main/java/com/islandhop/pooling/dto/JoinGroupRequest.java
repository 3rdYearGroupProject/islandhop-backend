package com.islandhop.pooling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for joining a public group.
 * Enhanced with email support and user profile information.
 */
@Data
public class JoinGroupRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @Email(message = "Valid email address is required")
    @NotBlank(message = "Email is required")
    private String userEmail;
    
    private String userName;
    
    private String message; // Optional message explaining why they want to join
    
    private Map<String, Object> userProfile; // User's profile information
}op.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for joining a public group.
 * Follows the same patterns as other request DTOs for consistency.
 */
@Data
public class JoinGroupRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private Map<String, Object> userProfile;
}
