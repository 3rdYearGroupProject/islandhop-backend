package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import com.islandhop.userservices.model.SupportProfile;
import com.islandhop.userservices.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/support")
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
@RequiredArgsConstructor
public class SupportController {

    private static final Logger logger = LoggerFactory.getLogger(SupportController.class);
    private final SupportService supportService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /support/login called with body: {}", requestBody);
        return ResponseEntity.ok(Map.of("message", "Support login endpoint"));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /support/health called");
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        logger.info("GET /support/session/validate called");
        return ResponseEntity.ok(Map.of("valid", true));
    }

    // Get profile details by email
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String email) {
        logger.info("GET /support/profile called for email: {}", email);
        SupportProfile profile = supportService.getProfileByEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    // Create profile (first time setup)
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @RequestParam("email") String email,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "contactNo", required = false) String contactNo,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        
        logger.info("POST /support/profile called for email: {}", email);
        
        try {
            SupportProfile created = supportService.createOrUpdateProfile(
                email, firstName, lastName, contactNo, address, profilePicture);
            
            if (created == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to create profile"));
            }
            
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating profile for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // Update profile details (including photo)
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam("email") String email,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "contactNo", required = false) String contactNo,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        
        logger.info("PUT /support/profile called for email: {}", email);
        
        try {
            SupportProfile updated = supportService.createOrUpdateProfile(
                email, firstName, lastName, contactNo, address, profilePicture);
            
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating profile for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // Change account status
    @PutMapping("/account/status")
    public ResponseEntity<?> changeStatus(@RequestBody Map<String, String> request) {
        logger.info("PUT /support/account/status called for email: {}", request.get("email"));
        String email = request.get("email");
        String status = request.get("status");
        boolean result = supportService.changeAccountStatus(email, status);
        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }
}