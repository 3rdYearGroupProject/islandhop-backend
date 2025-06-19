package com.islandhop.userservices.controller;

import com.islandhop.userservices.service.AdminService;
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
}
