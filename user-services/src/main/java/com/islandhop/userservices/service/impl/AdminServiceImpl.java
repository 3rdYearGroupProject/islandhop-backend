package com.islandhop.userservices.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.islandhop.userservices.model.AdminAccount;
import com.islandhop.userservices.repository.AdminAccountRepository;
import com.islandhop.userservices.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminAccountRepository adminAccountRepository;

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
    public boolean isAdmin(String email) {
        return adminAccountRepository.existsByEmail(email);
    }
}