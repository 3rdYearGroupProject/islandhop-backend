package com.example.firebaseuser.controller;

import com.example.firebaseuser.model.UserInfoDTO;
import com.example.firebaseuser.service.FirebaseUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Firebase user operations.
 */
@RestController
@RequestMapping("/firebase/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

    /**
     * Get Firebase UID by email and add member to group.
     */
    @PostMapping("/email-to-group")
    public ResponseEntity<?> addMemberToGroup(@RequestHeader("Authorization") String authToken, @RequestBody Map<String, String> requestBody) {
        logger.info("API POST /email-to-group");
        try {
            String email = requestBody.get("email");
            String groupId = requestBody.get("groupId");
            String requesterId = requestBody.get("requesterId");

            logger.debug("Processing request - Email: {}, GroupId: {}, RequesterId: {}", email, groupId, requesterId);

            // Get Firebase UID by email
            String firebaseUid = firebaseUserService.getUidByEmail(email);
            logger.debug("Firebase UID for email {}: {}", email, firebaseUid);

            // Prepare request for group addition
            Map<String, String> groupRequest = new HashMap<>();
            groupRequest.put("groupId", groupId);
            groupRequest.put("userId", firebaseUid);
            groupRequest.put("requesterId", requesterId);

            // Create headers with authorization token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(groupRequest, headers);

            RestTemplate restTemplate = new RestTemplate();
            String groupEndpoint = "http://localhost:8090/api/v1/chat/group/add-member";
            
            ResponseEntity<String> response = restTemplate.postForEntity(groupEndpoint, entity, String.class);

            logger.info("Group addition response: {}", response.getBody());
            return ResponseEntity.ok().body(response.getBody());
        } catch (Exception e) {
            logger.error("Error adding member to group: {}", e.getMessage());
            return ResponseEntity.status(500).body(error("Failed to add member to group: " + e.getMessage()));
        }
    }

    private static Map<String, String> error(String msg) {
        return Map.of("error", msg);
    }

    private static Map<String, String> success(String msg) {
        return Map.of("message", msg);
    }
}