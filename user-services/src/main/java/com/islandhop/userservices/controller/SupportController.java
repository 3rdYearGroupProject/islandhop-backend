package com.islandhop.userservices.controller;

import com.islandhop.userservices.model.SupportProfile;
import com.islandhop.userservices.model.SupportStatus;
import com.islandhop.userservices.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * REST controller for managing support user operations.
 * Handles endpoints under /support.
 */
@RestController
@RequestMapping("/support")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class SupportController {

    private static final Logger logger = LoggerFactory.getLogger(SupportController.class);
    private final SupportService supportService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /support/login called with body: {}", requestBody);
        // Implement support login logic here
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
        // Implement session validation logic for support
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

    // Update profile details
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        logger.info("PUT /support/profile called for email: {}", request.get("email"));
        SupportProfile updated = supportService.updateProfile(request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // Change account status and send email
    @PutMapping("/account/status")
    public ResponseEntity<?> changeStatus(@RequestBody Map<String, String> request) {
        logger.info("PUT /support/account/status called for email: {}", request.get("email"));
        String email = request.get("email");
        String status = request.get("status");
        boolean result = supportService.changeAccountStatus(email, status);
        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Status updated and email sent"));
    }

    // Set (create) profile details
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@RequestBody Map<String, String> request) {
        logger.info("POST /support/profile called for email: {}", request.get("email"));
        SupportProfile created = supportService.createProfile(request);
        if (created == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Profile already exists or invalid data"));
        }
        return ResponseEntity.ok(created);
    }
}