package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import com.islandhop.userservices.dto.UpdateUserStatusRequest;
import com.islandhop.userservices.dto.UserAccountResponse;
import com.islandhop.userservices.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
public class UniversalRoleController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalRoleController.class);
    private final UserService userService;

    public UniversalRoleController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/role")
    public ResponseEntity<?> getRole(HttpSession session) {
        Object email = session.getAttribute("userEmail");
        Object role = session.getAttribute("userRole");
        Object isAuthenticated = session.getAttribute("isAuthenticated");

        logger.info("GET /role called. Email: {}, Role: {}, Authenticated: {}", email, role, isAuthenticated);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null && role != null) {
            return ResponseEntity.ok(Map.of(
                "email", email,
                "role", role
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
    }

    /**
     * Get all user accounts in the system
     * @param session HTTP session for authentication
     * @return List of user accounts with their details
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        Object isAuthenticated = session.getAttribute("isAuthenticated");
        Object userRole = session.getAttribute("userRole");
        Object userEmail = session.getAttribute("userEmail");

        logger.info("GET /users called. Email: {}, Role: {}, Authenticated: {}", userEmail, userRole, isAuthenticated);

        if (!Boolean.TRUE.equals(isAuthenticated)) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        // Check if user has admin/support role to access all users
        if (!"SUPPORT".equalsIgnoreCase(String.valueOf(userRole)) && !"ADMIN".equalsIgnoreCase(String.valueOf(userRole))) {
            return ResponseEntity.status(403).body(Map.of("message", "Insufficient permissions"));
        }   

        try {
            List<UserAccountResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "users", users
            ));
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
        }
    }

    /**
     * Update user account status
     * @param request Request containing email and new status
     * @param session HTTP session for authentication
     * @return Response indicating success or failure
     */
    @PutMapping("/users/status")
    public ResponseEntity<?> updateUserStatus(@Valid @RequestBody UpdateUserStatusRequest request, HttpSession session) {
        Object isAuthenticated = session.getAttribute("isAuthenticated");
        Object userRole = session.getAttribute("userRole");
        Object userEmail = session.getAttribute("userEmail");

        logger.info("PUT /users/status called. Email: {}, Role: {}, Authenticated: {}, Target: {}, New Status: {}", 
                   userEmail, userRole, isAuthenticated, request.getEmail(), request.getStatus());

        if (!Boolean.TRUE.equals(isAuthenticated)) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        // Check if user has admin/support role to update user status
        if (!"SUPPORT".equalsIgnoreCase(String.valueOf(userRole)) && !"ADMIN".equalsIgnoreCase(String.valueOf(userRole))) {
            return ResponseEntity.status(403).body(Map.of("message", "Insufficient permissions"));
        }

        try {
            userService.updateUserStatus(request.getEmail(), request.getStatus());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User status updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for status update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating user status", e);
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
        }
    }
}
