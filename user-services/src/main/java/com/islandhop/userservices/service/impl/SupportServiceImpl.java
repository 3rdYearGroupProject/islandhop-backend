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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private static final Logger logger = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final SupportProfileRepository supportProfileRepository;
    private final SupportAccountRepository supportAccountRepository;
    private final EmailService emailService;

    public String getEmailFromIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getEmail();
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

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
            return null;
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

    @Override
    public String uploadProfilePhoto(String email, MultipartFile photo) {
        try {
            logger.info("Uploading profile photo for email: {}", email);
            
            // Validate file
            if (photo.isEmpty()) {
                logger.error("Empty file uploaded for email: {}", email);
                return null;
            }

            // Check file type
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.error("Invalid file type uploaded for email: {}. Content type: {}", email, contentType);
                return null;
            }

            // Check file size (limit to 5MB)
            if (photo.getSize() > 5 * 1024 * 1024) {
                logger.error("File too large for email: {}. Size: {} bytes", email, photo.getSize());
                return null;
            }

            // Create uploads directory if it doesn't exist
            String uploadDir = "uploads/profile-photos/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                logger.info("Created upload directory: {}, success: {}", uploadDir, created);
            }

            // Generate unique filename
            String originalFilename = photo.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = email.replace("@", "_").replace(".", "_") + "_" + 
                System.currentTimeMillis() + fileExtension;

            // Save file
            String filePath = uploadDir + filename;
            File destinationFile = new File(filePath);
            photo.transferTo(destinationFile);

            // Update profile with photo URL
            Optional<SupportProfile> profileOpt = supportProfileRepository.findByEmail(email);
            if (profileOpt.isPresent()) {
                SupportProfile profile = profileOpt.get();
                String photoUrl = "/uploads/profile-photos/" + filename;
                profile.setPhotoUrl(photoUrl);
                supportProfileRepository.save(profile);
                
                logger.info("Profile photo uploaded successfully for email: {}. URL: {}", email, photoUrl);
                return photoUrl;
            } else {
                logger.error("No profile found for email: {}", email);
                return null;
            }

        } catch (Exception e) {
            logger.error("Error uploading profile photo for email {}: {}", email, e.getMessage(), e);
            return null;
        }
    }
}