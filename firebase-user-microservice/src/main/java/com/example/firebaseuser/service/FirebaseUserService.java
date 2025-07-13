package com.example.firebaseuser.service;

import com.example.firebaseuser.model.UserInfoDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for Firebase user operations.
 */
@Service
public class FirebaseUserService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseUserService.class);

    /**
     * Get display name by UID.
     */
    public String getDisplayNameByUid(String uid) throws Exception {
        logger.info("Fetching display name for UID: {}", uid);
        UserRecord user = getUserByUid(uid);
        logger.debug("User found: {}", user.getDisplayName());
        return user.getDisplayName();
    }

    /**
     * Delete user by UID.
     */
    public void deleteUserByUid(String uid) throws Exception {
        logger.info("Deleting user with UID: {}", uid);
        getUserByUid(uid); // Throws if not found
        FirebaseAuth.getInstance().deleteUser(uid);
        logger.debug("User deleted: {}", uid);
    }

    /**
     * Disable (deactivate) user account.
     */
    public void deactivateUser(String uid) throws Exception {
        logger.info("Deactivating user with UID: {}", uid);
        UserRecord user = getUserByUid(uid);
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(true);
        FirebaseAuth.getInstance().updateUser(request);
        logger.debug("User deactivated: {}", uid);
    }

    /**
     * Enable (reactivate) user account.
     */
    public void activateUser(String uid) throws Exception {
        logger.info("Activating user with UID: {}", uid);
        UserRecord user = getUserByUid(uid);
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(false);
        FirebaseAuth.getInstance().updateUser(request);
        logger.debug("User activated: {}", uid);
    }

    /**
     * Get full user info by UID.
     */
    public UserInfoDTO getUserInfo(String uid) throws Exception {
        logger.info("Fetching user info for UID: {}", uid);
        UserRecord user = getUserByUid(uid);
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUid(user.getUid());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setDisabled(user.isDisabled());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setCreationTimestamp(user.getUserMetadata().getCreationTimestamp());
        dto.setLastSignInTimestamp(user.getUserMetadata().getLastSignInTimestamp());
        logger.debug("User info DTO created for UID: {}", uid);
        return dto;
    }

    /**
     * Get UID by email.
     */
    public String getUidByEmail(String email) throws Exception {
        logger.info("Fetching Firebase UID for email: {}", email);
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            logger.debug("Firebase UID for email {}: {}", email, userRecord.getUid());
            return userRecord.getUid();
        } catch (Exception e) {
            logger.error("Error fetching Firebase UID for email {}: {}", email, e.getMessage());
            throw new Exception("No Firebase account found for email: " + email);
        }
    }

    /**
     * Helper: Get user by UID, throws Exception if not found.
     */
    private UserRecord getUserByUid(String uid) throws Exception {
        try {
            logger.debug("Fetching user record for UID: {}", uid);
            return FirebaseAuth.getInstance().getUser(uid);
        } catch (Exception e) {
            logger.error("User not found for UID: {}", uid, e);
            throw new Exception("User not found for UID: " + uid);
        }
    }
}