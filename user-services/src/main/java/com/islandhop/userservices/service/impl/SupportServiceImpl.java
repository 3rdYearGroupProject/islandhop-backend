package com.islandhop.userservices.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.islandhop.userservices.model.SupportAccount;
import com.islandhop.userservices.repository.SupportAccountRepository;
import com.islandhop.userservices.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final SupportAccountRepository supportAccountRepository;

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
}