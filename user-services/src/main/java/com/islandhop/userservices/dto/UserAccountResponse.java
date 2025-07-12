package com.islandhop.userservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user account information
 * Contains user profile details for administrative purposes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountResponse {
    
    /**
     * User's first name
     */
    private String firstName;
    
    /**
     * User's last name
     */
    private String lastName;
    
    /**
     * User's email address
     */
    private String email;
    
    /**
     * URL to user's profile picture
     */
    private String profilePicUrl;
    
    /**
     * Account type: TOURIST, SUPPORT, DRIVER, GUIDE
     */
    private String accountType;
    
    /**
     * Account status: ACTIVE, DEACTIVATED, SUSPENDED, PENDING
     */
    private String status;
}