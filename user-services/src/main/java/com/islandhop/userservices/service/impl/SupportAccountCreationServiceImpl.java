package com.islandhop.userservices.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.islandhop.userservices.model.SupportStatus; // Use the model package SupportStatus
import com.islandhop.userservices.model.SupportAccount;
import com.islandhop.userservices.model.SupportProfile;
import com.islandhop.userservices.repository.SupportAccountRepository;
import com.islandhop.userservices.repository.SupportProfileRepository;
import com.islandhop.userservices.service.EmailService;
import com.islandhop.userservices.service.SupportAccountCreationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportAccountCreationServiceImpl implements SupportAccountCreationService {

    private static final Logger logger = LoggerFactory.getLogger(SupportAccountCreationServiceImpl.class);
    
    private final SupportAccountRepository supportAccountRepository;
    private final SupportProfileRepository supportProfileRepository;
    private final EmailService emailService;

    @Override
    public boolean createSupportAccount(String email) throws Exception {
        logger.info("Creating support account for email: {}", email);
        
        // Check if account already exists
        Optional<SupportAccount> existingAccount = supportAccountRepository.findByEmail(email);
        if (existingAccount.isPresent()) {
            logger.warn("Support account already exists for email: {}", email);
            return false;
        }

        // Generate random password
        String password = UUID.randomUUID().toString().substring(0, 12);
        logger.info("Generated password for support account: {}", email);

        try {
            // Create user in Firebase
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
            logger.info("Firebase user created successfully for email: {}", email);

            // Create SupportAccount in database
            SupportAccount account = new SupportAccount();
            account.setEmail(email);
            account.setStatus(SupportStatus.ACTIVE); // Use the existing SupportStatus
            supportAccountRepository.save(account);
            logger.info("Support account saved to database for email: {}", email);

            // Create basic SupportProfile in database
            SupportProfile profile = new SupportProfile();
            profile.setEmail(email);
            profile.setFirstName(null);  // Explicitly set to null for new accounts
            profile.setLastName(null);
            profile.setContactNo(null);
            profile.setAddress(null);
            profile.setProfilePicture(null);
            supportProfileRepository.save(profile);
            logger.info("Basic support profile created for email: {}", email);

            // Send credentials email
            sendCredentialsEmail(email, password);
            logger.info("Credentials email sent for email: {}", email);

            return true;

        } catch (Exception e) {
            logger.error("Error creating support account for email {}: {}", email, e.getMessage());
            
            // Cleanup: Try to delete Firebase user if it was created
            try {
                UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
                if (userRecord != null) {
                    FirebaseAuth.getInstance().deleteUser(userRecord.getUid());
                    logger.info("Cleaned up Firebase user for email: {}", email);
                }
            } catch (Exception cleanupException) {
                logger.warn("Failed to cleanup Firebase user for email {}: {}", email, cleanupException.getMessage());
            }
            
            throw e;
        }
    }

    private void sendCredentialsEmail(String email, String password) {
        try {
            String subject = "Your IslandHop Support Agent Account";
            String body = String.format(
                "Dear Support Agent,\n\n" +
                "Your support agent account has been created successfully.\n\n" +
                "Login Credentials:\n" +
                "Email: %s\n" +
                "Password: %s\n\n" +
                "Please log in to your account and complete your profile setup.\n" +
                "For security reasons, please change your password after your first login.\n\n" +
                "Best regards,\n" +
                "IslandHop Admin Team",
                email, password
            );
            
            emailService.sendEmail(email, subject, body);
            
        } catch (Exception e) {
            logger.error("Failed to send credentials email to {}: {}", email, e.getMessage());
            // Don't throw exception here as the account was created successfully
        }
    }
}