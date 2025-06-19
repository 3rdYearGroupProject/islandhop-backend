package com.islandhop.userservices.service;

import com.islandhop.userservices.dto.TouristRegistrationRequest;
import com.islandhop.userservices.model.TouristAccount;
import com.islandhop.userservices.model.TouristProfile;

public interface TouristService {
    // Creates a minimal account after Firebase auth (session-register)
    TouristAccount createTouristAccount(String email);

    // Completes the profile with additional details
    TouristProfile completeTouristProfile(String email, String firstName, String lastName, String nationality, java.util.List<String> languages);

    // Gets email from Firebase ID token
    String getEmailFromIdToken(String idToken);

    // (Optional) Other methods as needed for future development
}