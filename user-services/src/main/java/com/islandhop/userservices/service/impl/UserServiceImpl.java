package com.islandhop.userservices.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.islandhop.userservices.model.*;
import com.islandhop.userservices.repository.*;
import com.islandhop.userservices.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TouristAccountRepository touristAccountRepository;
    private final DriverAccountRepository driverAccountRepository;
    private final GuideAccountRepository guideAccountRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final SupportAccountRepository supportAccountRepository;
    
    // Profile repositories
    private final TouristProfileRepository touristProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final GuideProfileRepository guideProfileRepository;
    private final SupportProfileRepository supportProfileRepository;

    @Override
    public Map<String, Object> validateAndGetUserDetails(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            if (email == null) return null;

            // Check each user table for the email
            if (touristAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "tourist");
                details.put("email", email);
                
                // Get profile completion status
                TouristProfile profile = touristProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            if (driverAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "driver");
                details.put("email", email);
                
                // Get profile completion status
                DriverProfile profile = driverProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            if (guideAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "guide");
                details.put("email", email);
                
                // Get profile completion status
                GuideProfile profile = guideProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            if (adminAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "admin");
                details.put("email", email);
                return details;
            }
            if (supportAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "support");
                details.put("email", email);
                
                // Get profile completion status
                SupportProfile profile = supportProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            // Not found in any table
            return null;
        } catch (FirebaseAuthException e) {
            return null;
        }
    }
}