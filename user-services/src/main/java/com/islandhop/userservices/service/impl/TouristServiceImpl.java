package com.islandhop.userservices.service.impl;

import com.islandhop.userservices.model.TouristAccount;
import com.islandhop.userservices.model.TouristProfile;
import com.islandhop.userservices.model.TouristStatus;
import com.islandhop.userservices.repository.TouristAccountRepository;
import com.islandhop.userservices.repository.TouristProfileRepository;
import com.islandhop.userservices.service.TouristService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristServiceImpl implements TouristService {

    private final TouristAccountRepository accountRepository;
    private final TouristProfileRepository profileRepository;

    private static final Logger logger = LoggerFactory.getLogger(TouristServiceImpl.class);

    @Override
    @Transactional
    public TouristAccount createTouristAccount(String email) {
        logger.info("Creating tourist account for email: {}", email);
        TouristAccount account = new TouristAccount();
        account.setEmail(email);
        account.setStatus(TouristStatus.ACTIVE);
        TouristAccount saved = accountRepository.save(account);
        logger.info("Tourist account saved: {}", saved);
        return saved;
    }

    @Override
    @Transactional
    public TouristProfile completeTouristProfile(String email, String firstName, String lastName, String nationality, List<String> languages) {
        logger.info("Completing profile for email: {}", email);
        TouristProfile profile = new TouristProfile();
        profile.setEmail(email);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setNationality(nationality);
        profile.setLanguages(languages);
        TouristProfile saved = profileRepository.save(profile);
        logger.info("Tourist profile saved: {}", saved);
        return saved;
    }

    @Override
    public String getEmailFromIdToken(String idToken) {
        logger.info("Verifying Firebase ID token");
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            logger.info("Firebase token verified for email: {}", decodedToken.getEmail());
            return decodedToken.getEmail();
        } catch (FirebaseAuthException e) {
            logger.error("Firebase token verification failed", e);
            return null;
        }
    }
}