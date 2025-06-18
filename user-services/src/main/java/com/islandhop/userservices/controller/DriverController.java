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

        DriverAccount account = driverService.createDriverAccount(email);
        logger.info("Driver account created for email: {}", email);
        return ResponseEntity.ok(account);
    }

    /**
     * Completes the driver profile.
     */
    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody Map<String, Object> requestBody) {
        logger.info("POST /driver/complete-profile called with body: {}", requestBody);
        String email = (String) requestBody.get("email");
        String firstName = (String) requestBody.get("firstName");
        String lastName = (String) requestBody.get("lastName");
        String licenseNo = (String) requestBody.get("licenseNo");
        List<String> languages = (List<String>) requestBody.get("languages");

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile completion attempted for non-existent driver: {}", email);
            return ResponseEntity.badRequest().body("Driver account does not exist");
        }

        if (profileRepository.existsByEmail(email)) {
            logger.info("Profile already exists for driver: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Profile already completed");
        }

        DriverProfile profile = driverService.completeDriverProfile(email, firstName, lastName, licenseNo, languages);
        logger.info("Driver profile completed for: {}", email);
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

        session.setAttribute("driverEmail", email);
        session.setAttribute("isDriverAuthenticated", true);
        logger.info("Driver logged in: {}", email);

        return ResponseEntity.ok(Map.of("message", "Login successful", "email", email));
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
            return ResponseEntity.ok(Map.of("valid", true, "email", email));
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
