package com.islandhop.chat.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Firebase authentication operations.
 * Handles token verification and user authentication.
 */
@Service
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    @Autowired
    private FirebaseAuth firebaseAuth;

    /**
     * Verify Firebase ID token and return user information.
     * 
     * @param idToken The Firebase ID token to verify
     * @return FirebaseToken containing user information
     * @throws FirebaseAuthException if token verification fails
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("ID token cannot be null or empty");
        }

        try {
            // Remove "Bearer " prefix if present
            if (idToken.startsWith("Bearer ")) {
                idToken = idToken.substring(7);
            }

            logger.debug("Verifying Firebase ID token...");
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            
            logger.debug("Token verified successfully for user: {}", decodedToken.getUid());
            return decodedToken;
        } catch (FirebaseAuthException e) {
            logger.error("Firebase token verification failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during token verification: {}", e.getMessage());
            throw new RuntimeException("Token verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get user ID from Firebase token.
     * 
     * @param idToken The Firebase ID token
     * @return User ID (UID) from the token
     * @throws FirebaseAuthException if token verification fails
     */
    public String getUserIdFromToken(String idToken) throws FirebaseAuthException {
        FirebaseToken token = verifyIdToken(idToken);
        return token.getUid();
    }

    /**
     * Get user email from Firebase token.
     * 
     * @param idToken The Firebase ID token
     * @return User email from the token
     * @throws FirebaseAuthException if token verification fails
     */
    public String getUserEmailFromToken(String idToken) throws FirebaseAuthException {
        FirebaseToken token = verifyIdToken(idToken);
        return token.getEmail();
    }

    /**
     * Check if token is valid without throwing exceptions.
     * 
     * @param idToken The Firebase ID token to check
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String idToken) {
        try {
            verifyIdToken(idToken);
            return true;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user information from Firebase token safely.
     * 
     * @param idToken The Firebase ID token
     * @return UserInfo object with user details, or null if token is invalid
     */
    public UserInfo getUserInfo(String idToken) {
        try {
            FirebaseToken token = verifyIdToken(idToken);
            return UserInfo.builder()
                    .uid(token.getUid())
                    .email(token.getEmail())
                    .name(token.getName())
                    .picture(token.getPicture())
                    .emailVerified(token.isEmailVerified())
                    .build();
        } catch (Exception e) {
            logger.debug("Failed to extract user info: {}", e.getMessage());
            return null;
        }
    }

    /**
     * User information extracted from Firebase token.
     */
    public static class UserInfo {
        private String uid;
        private String email;
        private String name;
        private String picture;
        private boolean emailVerified;

        private UserInfo(Builder builder) {
            this.uid = builder.uid;
            this.email = builder.email;
            this.name = builder.name;
            this.picture = builder.picture;
            this.emailVerified = builder.emailVerified;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getUid() { return uid; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPicture() { return picture; }
        public boolean isEmailVerified() { return emailVerified; }

        public static class Builder {
            private String uid;
            private String email;
            private String name;
            private String picture;
            private boolean emailVerified;

            public Builder uid(String uid) { this.uid = uid; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder picture(String picture) { this.picture = picture; return this; }
            public Builder emailVerified(boolean emailVerified) { this.emailVerified = emailVerified; return this; }

            public UserInfo build() { return new UserInfo(this); }
        }
    }
}
