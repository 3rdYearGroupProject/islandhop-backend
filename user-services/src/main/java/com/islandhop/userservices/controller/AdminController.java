package com.islandhop.userservices.controller;

import com.islandhop.userservices.service.AdminService;
import com.islandhop.userservices.service.SupportAccountCreationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

/**
 * REST controller for managing admin user operations.
 * Handles endpoints under /admin.
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final SupportAccountCreationService supportAccountCreationService;

    /**
     * Admin login using Firebase ID token.
     *
     * @param requestBody Map with "idToken" field
     * @param session     HTTP session to store authentication state
     * @return ResponseEntity with login status
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /admin/login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");

        if (idToken == null) {
            logger.warn("Login attempt with missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        // Validate token and check admin privileges
        String email = adminService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during admin login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        if (!adminService.isAdmin(email)) {
            logger.warn("Unauthorized admin login attempt by: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not an admin user");
        }

        session.setAttribute("adminEmail", email);
        session.setAttribute("isAdminAuthenticated", true);
        logger.info("Admin logged in and session started: {}", email);

        return ResponseEntity.ok(Map.of("message", "Admin login successful", "email", email));
    }

    /**
     * Logs out the currently logged-in admin.
     *
     * @param session HTTP session to invalidate
     * @return ResponseEntity with logout status
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("adminEmail");
        session.invalidate();
        logger.info("Admin logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Admin logged out"));
    }

    /**
     * Validates the current admin session.
     *
     * @param session HTTP session
     * @return ResponseEntity indicating session validity
     */
    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        String email = (String) session.getAttribute("adminEmail");
        logger.info("GET /admin/session/validate called. Authenticated: {}, Email: {}", isAuthenticated, email);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null) {
            return ResponseEntity.ok(Map.of("valid", true, "email", email));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
    }

    /**
     * Health check endpoint.
     *
     * @return ResponseEntity with status OK
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /admin/health called");
        return ResponseEntity.ok("OK");
    }

    /**
     * Creates a support account and sends credentials via email.
     *
     * @param request Map containing "email" field
     * @return ResponseEntity with account creation status
     */
    @PostMapping("/create/support")
    public ResponseEntity<?> createSupportAccount(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("POST /admin/create/support called with email: {}", email);

        if (email == null) {
            logger.warn("Support account creation failed: Email is required");
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        try {
            logger.info("Checking if support account already exists for email: {}", email);
            boolean created = supportAccountCreationService.createSupportAccount(email);
            if (!created) {
                logger.warn("Support account creation failed: Account already exists for email: {}", email);
                return ResponseEntity.status(409).body(Map.of("message", "Account already exists"));
            }
            logger.info("Support account created and credentials emailed for email: {}", email);
            return ResponseEntity.ok(Map.of("message", "Support account created and credentials emailed"));
        } catch (Exception e) {
            logger.error("Error creating support account for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Registers a new admin session using a Firebase ID token.
     *
     * @param requestBody Map containing "idToken" and "role"
     * @param session     HTTP session to store authentication state
     * @return ResponseEntity with account info or error status
     */
    @PostMapping("/session-register")
    public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /admin/session-register called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        // Validate input
        if (idToken == null || role == null || !"admin".equalsIgnoreCase(role)) {
            logger.warn("Invalid session-register request: missing idToken or role");
            return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
        }

        // Extract email from Firebase token
        String email = adminService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during session-register");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }


        // Create new admin account
        var account = adminService.createAdminAccount(email);
        logger.info("Admin account created for email: {}", email);

        // Set session attributes
        session.setAttribute("adminEmail", email);
        session.setAttribute("isAdminAuthenticated", true);

        return ResponseEntity.ok(account);
    }
}
