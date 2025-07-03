package com.islandhop.userservices.service;

import com.islandhop.userservices.model.SupportProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface SupportService {
    
    /**
     * Get support profile by email
     */
    SupportProfile getProfileByEmail(String email);
    
    /**
     * Create or update support profile with optional photo
     */
    SupportProfile createOrUpdateProfile(String email, String firstName, String lastName, 
                                       String contactNo, String address, MultipartFile profilePicture);
    
    /**
     * Change support account status
     */
    boolean changeAccountStatus(String email, String status);
    
    /**
     * Create basic profile entry (for account creation)
     */
    SupportProfile createBasicProfile(String email);
}