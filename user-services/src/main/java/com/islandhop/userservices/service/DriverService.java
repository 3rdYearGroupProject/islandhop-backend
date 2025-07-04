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

        // Update personal information
        if (requestBody.containsKey("firstName")) {
            profile.setFirstName((String) requestBody.get("firstName"));
        }
        if (requestBody.containsKey("lastName")) {
            profile.setLastName((String) requestBody.get("lastName"));
        }
        if (requestBody.containsKey("phone")) {
            profile.setPhoneNumber((String) requestBody.get("phone"));
        }
        if (requestBody.containsKey("dateOfBirth")) {
            String dobStr = (String) requestBody.get("dateOfBirth");
            if (dobStr != null && !dobStr.trim().isEmpty()) {
                profile.setDateOfBirth(java.time.LocalDate.parse(dobStr));
            }
        }
        if (requestBody.containsKey("address")) {
            profile.setAddress((String) requestBody.get("address"));
        }
        if (requestBody.containsKey("emergencyContactName")) {
            profile.setEmergencyContactName((String) requestBody.get("emergencyContactName"));
        }
        if (requestBody.containsKey("emergencyContact")) {
            profile.setEmergencyContactNumber((String) requestBody.get("emergencyContact"));
        }
        if (requestBody.containsKey("profilePicture")) {
            profile.setProfilePictureUrl((String) requestBody.get("profilePicture"));
        }

        // Update trip preferences
        if (requestBody.containsKey("acceptPartialTrips")) {
            Object value = requestBody.get("acceptPartialTrips");
            if (value instanceof Integer) {
                profile.setAcceptPartialTrips((Integer) value);
            } else if (value instanceof Boolean) {
                profile.setAcceptPartialTrips(((Boolean) value) ? 1 : 0);
            }
        }
        if (requestBody.containsKey("autoAcceptTrips")) {
            Object value = requestBody.get("autoAcceptTrips");
            if (value instanceof Integer) {
                profile.setAutoAcceptTrips((Integer) value);
            } else if (value instanceof Boolean) {
                profile.setAutoAcceptTrips(((Boolean) value) ? 1 : 0);
            }
        }
        if (requestBody.containsKey("maxDistance")) {
            Object value = requestBody.get("maxDistance");
            if (value instanceof Integer) {
                profile.setMaximumTripDistance((Integer) value);
            } else if (value instanceof String) {
                try {
                    profile.setMaximumTripDistance(Integer.parseInt((String) value));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid maxDistance value: {}", value);
                }
            }
        }

        // Check if profile is complete
        if (isProfileComplete(profile)) {
            profile.setProfileCompletion(1);
        } else {
            profile.setProfileCompletion(0);
        }

        return profileRepository.save(profile);
    }

    private boolean isProfileComplete(DriverProfile profile) {
        return profile.getFirstName() != null && !profile.getFirstName().trim().isEmpty() &&
               profile.getLastName() != null && !profile.getLastName().trim().isEmpty() &&
               profile.getPhoneNumber() != null && !profile.getPhoneNumber().trim().isEmpty() &&
               profile.getDateOfBirth() != null &&
               profile.getAddress() != null && !profile.getAddress().trim().isEmpty() &&
               profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().trim().isEmpty() &&
               profile.getEmergencyContactNumber() != null && !profile.getEmergencyContactNumber().trim().isEmpty();
    }
}