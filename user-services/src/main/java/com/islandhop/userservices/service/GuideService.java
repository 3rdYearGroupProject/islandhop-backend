package com.islandhop.userservices.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.islandhop.userservices.model.GuideAccount;
import com.islandhop.userservices.model.GuideProfile;
import com.islandhop.userservices.repository.GuideAccountRepository;
import com.islandhop.userservices.repository.GuideProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuideService {

    private static final Logger logger = LoggerFactory.getLogger(GuideService.class);

    private final GuideAccountRepository accountRepository;
    private final GuideProfileRepository profileRepository;

    public String getEmailFromIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getEmail();
        } catch (Exception e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage());
            return null;
        }
    }

    public GuideAccount createGuideAccount(String email) {
        GuideAccount account = GuideAccount.builder()
            .email(email)
            .status("ACTIVE")
            .build();
        return accountRepository.save(account);
    }

    public GuideProfile createBasicGuideProfile(String email) {
        GuideProfile profile = GuideProfile.builder()
            .email(email)
            .profileCompletion(0)
            .availabilityStatus("Available")
            .build();
        return profileRepository.save(profile);
    }

    @SuppressWarnings("unchecked")
    public GuideProfile updateGuideProfile(String email, Map<String, Object> requestBody) {
        GuideProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            throw new RuntimeException("Guide profile not found");
        }

        // Update profile fields from request body
        if (requestBody.containsKey("firstName")) {
            profile.setFirstName((String) requestBody.get("firstName"));
        }
        if (requestBody.containsKey("lastName")) {
            profile.setLastName((String) requestBody.get("lastName"));
        }
        if (requestBody.containsKey("contactNumber")) {
            profile.setContactNumber((String) requestBody.get("contactNumber"));
        }
        if (requestBody.containsKey("nicPassport")) {
            profile.setNicPassport((String) requestBody.get("nicPassport"));
        }
        if (requestBody.containsKey("nationality")) {
            profile.setNationality((String) requestBody.get("nationality"));
        }
        if (requestBody.containsKey("dateOfBirth")) {
            String dateStr = (String) requestBody.get("dateOfBirth");
            profile.setDateOfBirth(LocalDate.parse(dateStr));
        }
        if (requestBody.containsKey("yearsOfExperience")) {
            profile.setYearsOfExperience((Integer) requestBody.get("yearsOfExperience"));
        }
        if (requestBody.containsKey("specializations")) {
            profile.setSpecializations((List<String>) requestBody.get("specializations"));
        }
        if (requestBody.containsKey("spokenLanguages")) {
            profile.setSpokenLanguages((List<String>) requestBody.get("spokenLanguages"));
        }
        if (requestBody.containsKey("guideLicenseNumber")) {
            profile.setGuideLicenseNumber((String) requestBody.get("guideLicenseNumber"));
        }
        if (requestBody.containsKey("touristBoardRegistration")) {
            profile.setTouristBoardRegistration((String) requestBody.get("touristBoardRegistration"));
        }
        if (requestBody.containsKey("baseLocation")) {
            profile.setBaseLocation((String) requestBody.get("baseLocation"));
        }
        if (requestBody.containsKey("serviceAreas")) {
            profile.setServiceAreas((List<String>) requestBody.get("serviceAreas"));
        }
        if (requestBody.containsKey("availabilityStatus")) {
            profile.setAvailabilityStatus((String) requestBody.get("availabilityStatus"));
        }
        if (requestBody.containsKey("bio")) {
            profile.setBio((String) requestBody.get("bio"));
        }
        if (requestBody.containsKey("hourlyRate")) {
            profile.setHourlyRate((Double) requestBody.get("hourlyRate"));
        }
        if (requestBody.containsKey("dailyRate")) {
            profile.setDailyRate((Double) requestBody.get("dailyRate"));
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

    private boolean isProfileComplete(GuideProfile profile) {
        return profile.getFirstName() != null && !profile.getFirstName().trim().isEmpty() &&
               profile.getLastName() != null && !profile.getLastName().trim().isEmpty() &&
               profile.getContactNumber() != null && !profile.getContactNumber().trim().isEmpty() &&
               profile.getNicPassport() != null && !profile.getNicPassport().trim().isEmpty() &&
               profile.getGuideLicenseNumber() != null && !profile.getGuideLicenseNumber().trim().isEmpty() &&
               profile.getBaseLocation() != null && !profile.getBaseLocation().trim().isEmpty() &&
               profile.getYearsOfExperience() != null && profile.getYearsOfExperience() >= 0 &&
               profile.getHourlyRate() != null && profile.getHourlyRate() > 0 &&
               profile.getDailyRate() != null && profile.getDailyRate() > 0 &&
               profile.getSpokenLanguages() != null && !profile.getSpokenLanguages().isEmpty() &&
               profile.getSpecializations() != null && !profile.getSpecializations().isEmpty();
    }
}