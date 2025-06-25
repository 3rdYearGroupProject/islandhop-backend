package com.islandhop.userservices.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.islandhop.userservices.model.SupportAccount;
import com.islandhop.userservices.model.SupportProfile;
import com.islandhop.userservices.model.SupportStatus;
import com.islandhop.userservices.repository.SupportAccountRepository;
import com.islandhop.userservices.repository.SupportProfileRepository;
import com.islandhop.userservices.service.EmailService;
import com.islandhop.userservices.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private static final Logger logger = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final SupportProfileRepository supportProfileRepository;
    private final SupportAccountRepository supportAccountRepository;
    private final EmailService emailService;

    @Override
    public String getEmailFromIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getEmail();
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

    @Override
    public boolean isSupport(String email) {
        return supportAccountRepository.existsByEmail(email);
    }

    @Override
    public SupportProfile getProfileByEmail(String email) {
        return supportProfileRepository.findByEmail(email).orElse(null);
    }

    @Override
    public SupportProfile updateProfile(Map<String, String> request) {
        String email = request.get("email");
        Optional<SupportProfile> optionalProfile = supportProfileRepository.findByEmail(email);
        if (optionalProfile.isEmpty()) return null;
        SupportProfile profile = optionalProfile.get();

        profile.setFirstName(request.getOrDefault("firstName", profile.getFirstName()));
        profile.setLastName(request.getOrDefault("lastName", profile.getLastName()));
        profile.setContactNo(request.getOrDefault("contactNo", profile.getContactNo()));
        profile.setAddress(request.getOrDefault("address", profile.getAddress()));
        profile.setProfilePicture(request.getOrDefault("profilePicture", profile.getProfilePicture()));

        return supportProfileRepository.save(profile);
    }

    @Override
    public boolean changeAccountStatus(String email, String status) {
        Optional<SupportAccount> optionalAccount = supportAccountRepository.findByEmail(email);
        if (optionalAccount.isEmpty()) return false;
        SupportAccount account = optionalAccount.get();

        try {
            SupportStatus newStatus = SupportStatus.valueOf(status.toUpperCase());
            account.setStatus(newStatus);
            supportAccountRepository.save(account);

            // Send email notification
            String subject = "Account Status Changed";
            String body = "Dear Support Agent,\n\nYour account status has been changed to: " + newStatus + ".\n\nRegards,\nIslandHop Team";
            emailService.sendEmail(email, subject, body);

            logger.info("Status changed and email sent to {}", email);
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status value: {}", status);
            return false;
        }
    }

    @Override
    public SupportProfile createProfile(Map<String, String> request) {
        String email = request.get("email");
        if (email == null || supportProfileRepository.findByEmail(email).isPresent()) {
            return null; // Already exists or invalid
        }
        SupportProfile profile = new SupportProfile();
        profile.setEmail(email);
        profile.setFirstName(request.get("firstName"));
        profile.setLastName(request.get("lastName"));
        profile.setContactNo(request.get("contactNo"));
        profile.setAddress(request.get("address"));
        profile.setProfilePicture(request.get("profilePicture"));
        return supportProfileRepository.save(profile);
    }
}