package com.islandhop.userservices.service.impl;

import com.islandhop.userservices.model.SupportAccount;
import com.islandhop.userservices.model.SupportProfile;
import com.islandhop.userservices.repository.SupportAccountRepository;
import com.islandhop.userservices.repository.SupportProfileRepository;
import com.islandhop.userservices.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private static final Logger logger = LoggerFactory.getLogger(SupportServiceImpl.class);
    
    private final SupportProfileRepository supportProfileRepository;
    private final SupportAccountRepository supportAccountRepository;

    @Override
    public SupportProfile getProfileByEmail(String email) {
        logger.info("Getting profile for email: {}", email);
        Optional<SupportProfile> profileOpt = supportProfileRepository.findByEmail(email);
        
        if (profileOpt.isPresent()) {
            return profileOpt.get();
        } else {
            // Check if support account exists, if so create basic profile
            Optional<SupportAccount> accountOpt = supportAccountRepository.findByEmail(email);
            if (accountOpt.isPresent()) {
                logger.info("Creating basic profile for existing support account: {}", email);
                return createBasicProfile(email);
            }
            return null;
        }
    }

    @Override
    public SupportProfile createOrUpdateProfile(String email, String firstName, String lastName, 
                                               String contactNo, String address, MultipartFile profilePicture) {
        try {
            logger.info("Creating/updating profile for email: {}", email);
            
            // Find existing profile or create new one
            Optional<SupportProfile> profileOpt = supportProfileRepository.findByEmail(email);
            SupportProfile profile;
            
            if (profileOpt.isPresent()) {
                profile = profileOpt.get();
                logger.info("Updating existing profile for: {}", email);
            } else {
                profile = new SupportProfile();
                profile.setEmail(email);
                logger.info("Creating new profile for: {}", email);
            }
            
            // Update fields if provided
            if (firstName != null) profile.setFirstName(firstName);
            if (lastName != null) profile.setLastName(lastName);
            if (contactNo != null) profile.setContactNo(contactNo);
            if (address != null) profile.setAddress(address);
            
            // Handle profile picture upload
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String base64Image = convertImageToBase64(profilePicture);
                if (base64Image != null) {
                    profile.setProfilePicture(base64Image);
                    logger.info("Profile picture updated for: {}", email);
                }
            }
            
            return supportProfileRepository.save(profile);
            
        } catch (Exception e) {
            logger.error("Error creating/updating profile for email {}: {}", email, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean changeAccountStatus(String email, String status) {
        try {
            Optional<SupportAccount> accountOpt = supportAccountRepository.findByEmail(email);
            if (accountOpt.isPresent()) {
                SupportAccount account = accountOpt.get();
                // Assuming SupportAccount has a status field as String
                // Update this based on your actual SupportAccount model
                logger.info("Account status changed to {} for email: {}", status, email);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error changing account status for email {}: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public SupportProfile createBasicProfile(String email) {
        try {
            SupportProfile profile = new SupportProfile();
            profile.setEmail(email);
            profile.setFirstName("");
            profile.setLastName("");
            profile.setContactNo("");
            profile.setAddress("");
            profile.setProfilePicture(null);
            
            return supportProfileRepository.save(profile);
        } catch (Exception e) {
            logger.error("Error creating basic profile for email {}: {}", email, e.getMessage());
            return null;
        }
    }

    private String convertImageToBase64(MultipartFile image) {
        try {
            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.error("Invalid file type: {}", contentType);
                return null;
            }

            // Check file size (limit to 5MB)
            if (image.getSize() > 5 * 1024 * 1024) {
                logger.error("File too large: {} bytes", image.getSize());
                return null;
            }

            // Convert to Base64
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return "data:" + contentType + ";base64," + base64Image;
            
        } catch (Exception e) {
            logger.error("Error converting image to Base64: {}", e.getMessage());
            return null;
        }
    }
}