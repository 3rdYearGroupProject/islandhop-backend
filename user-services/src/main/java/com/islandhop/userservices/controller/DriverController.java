package com.islandhop.userservices.controller;

import com.islandhop.userservices.model.DriverAccount;
import com.islandhop.userservices.model.DriverProfile;
import com.islandhop.userservices.repository.DriverAccountRepository;
import com.islandhop.userservices.repository.DriverProfileRepository;
import com.islandhop.userservices.service.DriverService;
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
 * REST controller for managing driver user operations.
 * Handles endpoints under /driver.
 */
@RestController
@RequestMapping("/driver")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    private final DriverAccountRepository accountRepository;
    private final DriverProfileRepository profileRepository;
    private final DriverService driverService;

    /**
     * Registers a new driver account from Firebase ID token.
     */
    @PostMapping("/session-register")
    public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody) {
        logger.info("POST /driver/session-register called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        if (idToken == null || role == null || !"driver".equalsIgnoreCase(role)) {
            logger.warn("Invalid session-register request for driver: {}", requestBody);
            return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
        }

        String email = driverService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during driver session-register");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        if (accountRepository.existsByEmail(email)) {
            logger.info("Driver already registered: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        // Create driver account and basic profile
        DriverAccount account = driverService.createDriverAccount(email);
        DriverProfile profile = driverService.createBasicDriverProfile(email);
        
        logger.info("Driver account and basic profile created for email: {}", email);
        return ResponseEntity.ok(Map.of(
            "account", account,
            "profile", profile,
            "message", "Driver account created successfully"
        ));
    }

    /**
     * Updates the driver profile with complete information.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        logger.info("PUT /driver/profile called with body: {}", requestBody);
        
        // Get email from session
        String email = (String) session.getAttribute("driverEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile update attempted for non-existent driver: {}", email);
            return ResponseEntity.badRequest().body("Driver account does not exist");
        }

        try {
            DriverProfile profile = driverService.updateDriverProfile(email, requestBody);
            logger.info("Driver profile updated for: {}", email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error updating driver profile for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Gets the driver profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        String email = (String) session.getAttribute("driverEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        DriverProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }

    /**
     * Authenticates a driver and starts a session.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /driver/login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");

        if (idToken == null) {
            logger.warn("Driver login failed: missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        String email = driverService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Driver login failed: invalid Firebase token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        // Check if driver account exists
        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Driver login failed: account not found for {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Driver account not found");
        }

        session.setAttribute("driverEmail", email);
        session.setAttribute("isDriverAuthenticated", true);
        logger.info("Driver logged in: {}", email);

        // Get profile completion status
        DriverProfile profile = profileRepository.findByEmail(email);
        boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;

        return ResponseEntity.ok(Map.of(
            "message", "Login successful", 
            "email", email,
            "profileComplete", profileComplete
        ));
    }

    /**
     * Logs out the current driver.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("driverEmail");
        session.invalidate();
        logger.info("Driver logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /**
     * Validates the driver session.
     */
    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isDriverAuthenticated");
        String email = (String) session.getAttribute("driverEmail");

        logger.info("GET /driver/session/validate called. Authenticated: {}, Email: {}", isAuthenticated, email);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null) {
            // Get profile completion status
            DriverProfile profile = profileRepository.findByEmail(email);
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
        logger.info("GET /driver/health called");
        return ResponseEntity.ok("OK");
    }
}
