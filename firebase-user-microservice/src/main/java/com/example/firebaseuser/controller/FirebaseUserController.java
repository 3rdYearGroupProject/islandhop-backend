package com.example.firebaseuser.controller;

import com.example.firebaseuser.model.UserInfoDTO;
import com.example.firebaseuser.service.FirebaseUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Firebase user operations.
 */
@RestController
@RequestMapping("/firebase/user")
public class FirebaseUserController {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseUserController.class);
    private final FirebaseUserService firebaseUserService;

    public FirebaseUserController(FirebaseUserService firebaseUserService) {
        this.firebaseUserService = firebaseUserService;
    }

    /**
     * Get user display name by UID.
     */
    @GetMapping("/display-name/{uid}")
    public ResponseEntity<?> getDisplayName(@PathVariable String uid) {
        logger.info("API GET /display-name/{}", uid);
        try {
            String displayName = firebaseUserService.getDisplayNameByUid(uid);
            return ResponseEntity.ok().body(displayName);
        } catch (Exception e) {
            logger.error("Error fetching display name: {}", e.getMessage());
            return ResponseEntity.status(404).body(error("User not found"));
        }
    }

    /**
     * Delete user account by UID.
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity<?> deleteUser(@PathVariable String uid) {
        logger.info("API DELETE /{}", uid);
        try {
            firebaseUserService.deleteUserByUid(uid);
            return ResponseEntity.ok().body(success("User deleted"));
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(404).body(error("User not found"));
        }
    }

    /**
     * Deactivate (disable) user account.
     */
    @PutMapping("/deactivate/{uid}")
    public ResponseEntity<?> deactivateUser(@PathVariable String uid) {
        logger.info("API PUT /deactivate/{}", uid);
        try {
            firebaseUserService.deactivateUser(uid);
            return ResponseEntity.ok().body(success("User deactivated"));
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage());
            return ResponseEntity.status(404).body(error("User not found"));
        }
    }

    /**
     * Reactivate (enable) user account.
     */
    @PutMapping("/activate/{uid}")
    public ResponseEntity<?> activateUser(@PathVariable String uid) {
        logger.info("API PUT /activate/{}", uid);
        try {
            firebaseUserService.activateUser(uid);
            return ResponseEntity.ok().body(success("User activated"));
        } catch (Exception e) {
            logger.error("Error activating user: {}", e.getMessage());
            return ResponseEntity.status(404).body(error("User not found"));
        }
    }

    /**
     * Get full user info by UID.
     */
    @GetMapping("/info/{uid}")
    public ResponseEntity<?> getUserInfo(@PathVariable String uid) {
        logger.info("API GET /info/{}", uid);
        try {
            UserInfoDTO dto = firebaseUserService.getUserInfo(uid);
            return ResponseEntity.ok().body(dto);
        } catch (Exception e) {
            logger.error("Error fetching user info: {}", e.getMessage());
            return ResponseEntity.status(404).body(error("User not found"));
        }
    }

    private static java.util.Map<String, String> error(String msg) {
        return java.util.Map.of("error", msg);
    }
    private static java.util.Map<String, String> success(String msg) {
        return java.util.Map.of("message", msg);
    }
}