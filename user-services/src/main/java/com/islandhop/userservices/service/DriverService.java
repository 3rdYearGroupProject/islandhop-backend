package com.islandhop.userservices.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.islandhop.userservices.model.DriverAccount;
import com.islandhop.userservices.model.DriverProfile;
import com.islandhop.userservices.repository.DriverAccountRepository;
import com.islandhop.userservices.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverAccountRepository accountRepository;
    private final DriverProfileRepository profileRepository;

    public String getEmailFromIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getEmail();
        } catch (Exception e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage());
            return null;
        }
    }

    public DriverAccount createDriverAccount(String email) {
        DriverAccount account = new DriverAccount();
        account.setEmail(email);
        account.setStatus("ACTIVE");
        return accountRepository.save(account);
    }

    public DriverProfile createBasicDriverProfile(String email) {
        DriverProfile profile = DriverProfile.builder()
            .email(email)
            .profileCompletion(0)
            .build();
        return profileRepository.save(profile);
    }

    public DriverProfile updateDriverProfile(String email, Map<String, Object> requestBody) {
        DriverProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            throw new RuntimeException("Driver profile not found");
        }

        // Update profile fields from request body
        if (requestBody.containsKey("fullName")) {
            profile.setFullName((String) requestBody.get("fullName"));
        }
        if (requestBody.containsKey("contactNumber")) {
            profile.setContactNumber((String) requestBody.get("contactNumber"));
        }
        if (requestBody.containsKey("nicPassport")) {
            profile.setNicPassport((String) requestBody.get("nicPassport"));
        }
        if (requestBody.containsKey("bodyType")) {
            profile.setBodyType((String) requestBody.get("bodyType"));
        }
        if (requestBody.containsKey("acAvailable")) {
            profile.setAcAvailable((String) requestBody.get("acAvailable"));
        }
        if (requestBody.containsKey("numberOfSeats")) {
            profile.setNumberOfSeats((Integer) requestBody.get("numberOfSeats"));
        }
        if (requestBody.containsKey("vehicleNumber")) {
            profile.setVehicleNumber((String) requestBody.get("vehicleNumber"));
        }
        if (requestBody.containsKey("vehicleType")) {
            profile.setVehicleType((String) requestBody.get("vehicleType"));
        }
        if (requestBody.containsKey("profilePictureUrl")) {
            profile.setProfilePictureUrl((String) requestBody.get("profilePictureUrl"));
        }

        // Check if profile is complete
        if (isProfileComplete(profile)) {
            profile.setProfileCompletion(1);
        }

        return profileRepository.save(profile);
    }

    private boolean isProfileComplete(DriverProfile profile) {
        return profile.getFullName() != null && !profile.getFullName().trim().isEmpty() &&
               profile.getContactNumber() != null && !profile.getContactNumber().trim().isEmpty() &&
               profile.getNicPassport() != null && !profile.getNicPassport().trim().isEmpty() &&
               profile.getVehicleType() != null && !profile.getVehicleType().trim().isEmpty() &&
               profile.getVehicleNumber() != null && !profile.getVehicleNumber().trim().isEmpty() &&
               profile.getBodyType() != null && !profile.getBodyType().trim().isEmpty() &&
               profile.getAcAvailable() != null && !profile.getAcAvailable().trim().isEmpty() &&
               profile.getNumberOfSeats() != null && profile.getNumberOfSeats() > 0;
    }
}