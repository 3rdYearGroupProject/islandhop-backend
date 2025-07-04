package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import com.islandhop.userservices.model.TouristAccount;
import com.islandhop.userservices.model.TouristProfile;
import com.islandhop.userservices.model.TouristStatus;
import com.islandhop.userservices.repository.TouristAccountRepository;
import com.islandhop.userservices.repository.TouristProfileRepository;
import com.islandhop.userservices.service.TouristService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

/**
 * REST controller for managing tourist user operations such as registration,
 * profile completion, authentication, and session management.
 * 
 * Handles endpoints under /tourist.
 */
@RestController
@RequestMapping("/tourist")
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
@RequiredArgsConstructor
public class TouristController {

    private static final Logger logger = LoggerFactory.getLogger(TouristController.class);

    private final TouristAccountRepository accountRepository;
    private final TouristProfileRepository profileRepository;
    private final TouristService touristService;

    /**
     * Registers a new tourist session using a Firebase ID token.
     * 
     * @param requestBody Map containing "idToken" and "role"
     * @return ResponseEntity with account info or error status
     */
    @PostMapping("/session-register")
    public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody) {
        logger.info("POST /tourist/session-register called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        // Validate input
        if (idToken == null || role == null || !"tourist".equalsIgnoreCase(role)) {
            logger.warn("Invalid session-register request: missing idToken or role");
            return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
        }

        // Extract email from Firebase token
        String email = touristService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during session-register");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        // Check for existing account
        if (accountRepository.existsByEmail(email)) {
            logger.info("Attempt to register already existing email: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        // Create new account
        TouristAccount account = touristService.createTouristAccount(email);
        logger.info("Tourist account created for email: {}", email);
        return ResponseEntity.ok(account);
    }

    /**
     * Completes the tourist profile with additional user details.
     * 
     * @param requestBody Map containing profile fields
     * @return ResponseEntity with profile info or error status
     */
    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody Map<String, Object> requestBody) {
        logger.info("POST /tourist/complete-profile called with body: {}", requestBody);
        String email = (String) requestBody.get("email");
        String firstName = (String) requestBody.get("firstName");
        String lastName = (String) requestBody.get("lastName");
        String nationality = (String) requestBody.get("nationality");
        List<String> languages = (List<String>) requestBody.get("languages");

        // Ensure account exists
        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile completion attempted for non-existent account: {}", email);
            return ResponseEntity.badRequest().body("Account does not exist");
        }

        // Prevent duplicate profile completion
        if (profileRepository.existsByEmail(email)) {
            logger.info("Profile already completed for email: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Profile already completed");
        }

        // Complete profile
        TouristProfile profile = touristService.completeTouristProfile(email, firstName, lastName, nationality, languages);
        logger.info("Tourist profile completed for email: {}", email);
        return ResponseEntity.ok(profile);
    }

    /**
     * Authenticates a tourist using a Firebase ID token and starts a session.
     * 
     * @param requestBody Map containing "idToken"
     * @param session HTTP session for storing authentication state
     * @return ResponseEntity with login status or error
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /tourist/login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        if (idToken == null) {
            logger.warn("Login attempt with missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        // Validate token and extract email
        String email = touristService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Login failed: invalid Firebase token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        // Set session attributes
        session.setAttribute("userEmail", email);
        session.setAttribute("isAuthenticated", true);
        logger.info("User logged in and session started: {}", email);

        return ResponseEntity.ok(Map.of("message", "Login successful", "email", email));
    }

    /**
     * Logs out the current tourist by invalidating the session.
     * 
     * @param session HTTP session to invalidate
     * @return ResponseEntity with logout status
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        session.invalidate();
        logger.info("User logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /**
     * Retrieves the currently authenticated tourist's email from the session.
     * 
     * @param session HTTP session
     * @return ResponseEntity with user info or unauthorized status
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String email = (String) session.getAttribute("userEmail");
        logger.info("GET /tourist/me called. Authenticated: {}, Email: {}", isAuthenticated, email);
        if (isAuthenticated != null && isAuthenticated && email != null) {
            return ResponseEntity.ok(Map.of("email", email));
        } else {
            logger.warn("Unauthorized access to /tourist/me");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    }

    /**
     * Health check endpoint for the tourist service.
     * 
     * @return ResponseEntity with "OK" if service is running
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /tourist/health called");
        return ResponseEntity.ok("OK");
    }

    /**
     * Validates the current session for authentication status.
     * 
     * @param session HTTP session
     * @return ResponseEntity indicating session validity and user email if valid
     */
    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String email = (String) session.getAttribute("userEmail");
        logger.info("GET /tourist/session/validate called. Authenticated: {}, Email: {}", isAuthenticated, email);
        if (isAuthenticated != null && isAuthenticated && email != null) {
            return ResponseEntity.ok(Map.of("valid", true, "email", email));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
    }
}