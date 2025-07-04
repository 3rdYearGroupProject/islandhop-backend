package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import com.islandhop.userservices.model.GuideAccount;
import com.islandhop.userservices.model.GuideProfile;
import com.islandhop.userservices.repository.GuideAccountRepository;
import com.islandhop.userservices.repository.GuideProfileRepository;
import com.islandhop.userservices.service.GuideService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing guide user operations.
 * Handles endpoints under /guide.
 */
@RestController
@RequestMapping("/guide")
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
@RequiredArgsConstructor
public class GuideController {

    private static final Logger logger = LoggerFactory.getLogger(GuideController.class);

    private final GuideAccountRepository accountRepository;
    private final GuideProfileRepository profileRepository;
    private final GuideService guideService;

    /**
     * Registers a new guide using a Firebase ID token.
     */
    @PostMapping("/session-register")
    public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody) {
        logger.info("POST /guide/session-register called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        if (idToken == null || role == null || !"guide".equalsIgnoreCase(role)) {
            logger.warn("Invalid session-register request for guide: {}", requestBody);
            return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
        }

        String email = guideService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during guide session-register");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        if (accountRepository.existsByEmail(email)) {
            logger.info("Guide already registered: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        // Create guide account and basic profile
        GuideAccount account = guideService.createGuideAccount(email);
        GuideProfile profile = guideService.createBasicGuideProfile(email);
        
        logger.info("Guide account and basic profile created for email: {}", email);
        return ResponseEntity.ok(Map.of(
            "account", account,
            "profile", profile,
            "message", "Guide account created successfully"
        ));
    }

    /**
     * Updates the guide profile with complete information.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        logger.info("PUT /guide/profile called with body: {}", requestBody);
        
        // Get email from session
        String email = (String) session.getAttribute("guideEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile update attempted for non-existent guide: {}", email);
            return ResponseEntity.badRequest().body("Guide account does not exist");
        }

        try {
            GuideProfile profile = guideService.updateGuideProfile(email, requestBody);
            logger.info("Guide profile updated for: {}", email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error updating guide profile for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Gets the guide profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        String email = (String) session.getAttribute("guideEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        GuideProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }

    /**
     * Authenticates a guide and starts a session.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /guide/login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");

        if (idToken == null) {
            logger.warn("Guide login failed: missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        String email = guideService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Guide login failed: invalid Firebase token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        // Check if guide account exists
        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Guide login failed: account not found for {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guide account not found");
        }

        session.setAttribute("guideEmail", email);
        session.setAttribute("isGuideAuthenticated", true);
        logger.info("Guide logged in: {}", email);

        // Get profile completion status
        GuideProfile profile = profileRepository.findByEmail(email);
        boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;

        return ResponseEntity.ok(Map.of(
            "message", "Login successful", 
            "email", email,
            "profileComplete", profileComplete
        ));
    }

    /**
     * Logs out the current guide.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("guideEmail");
        session.invalidate();
        logger.info("Guide logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /**
     * Validates the guide session.
     */
    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isGuideAuthenticated");
        String email = (String) session.getAttribute("guideEmail");

        logger.info("GET /guide/session/validate called. Authenticated: {}, Email: {}", isAuthenticated, email);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null) {
            // Get profile completion status
            GuideProfile profile = profileRepository.findByEmail(email);
            boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
            
            return ResponseEntity.ok(Map.of(
                "valid", true, 
                "email", email,
                "profileComplete", profileComplete
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /guide/health called");
        return ResponseEntity.ok("OK");
    }
}
