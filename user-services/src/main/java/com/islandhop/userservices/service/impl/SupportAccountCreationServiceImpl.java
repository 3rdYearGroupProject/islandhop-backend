package com.islandhop.userservices.service.impl;import com.google.firebase.auth.FirebaseAuth;import com.google.firebase.auth.UserRecord;import com.islandhop.userservices.model.SupportAccount;
import com.islandhop.userservices.model.SupportProfile;
import com.islandhop.userservices.model.SupportStatus;
import com.islandhop.userservices.repository.SupportAccountRepository;
import com.islandhop.userservices.repository.SupportProfileRepository;
import com.islandhop.userservices.service.EmailService;
import com.islandhop.userservices.service.SupportAccountCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportAccountCreationServiceImpl implements SupportAccountCreationService {

    private final SupportAccountRepository supportAccountRepository;
    private final SupportProfileRepository supportProfileRepository;
    private final EmailService emailService;

    @Override
    public boolean createSupportAccount(String email) throws Exception {
        // Check if account already exists
        if (supportAccountRepository.findByEmail(email).isPresent()) {
            return false;
        }

        // Generate random password
        String password = UUID.randomUUID().toString().substring(0, 10);

        // Create user in Firebase
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false)
                .setDisabled(false);

        FirebaseAuth.getInstance().createUser(createRequest);

        // Create SupportAccount
        SupportAccount account = new SupportAccount();
        account.setEmail(email);
        account.setStatus(SupportStatus.ACTIVE);
        supportAccountRepository.save(account);

        // Create SupportProfile with only email
        SupportProfile profile = new SupportProfile();
        profile.setEmail(email);
        profile.setFirstName(null);
        profile.setLastName(null);
        profile.setContactNo(null);
        profile.setAddress(null);
        profile.setProfilePicture(null);
        supportProfileRepository.save(profile);

        // Email credentials
        String subject = "Your Support Agent Account";
        String body = "Dear Support Agent,\n\nYour account has been created.\nEmail: " + email + "\nPassword: " + password + "\n\nPlease log in and update your profile.\n\nRegards,\nIslandHop Team";
        emailService.sendEmail(email, subject, body);

        return true;
    }
}