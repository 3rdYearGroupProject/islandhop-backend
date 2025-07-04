package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
public class UniversalRoleController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalRoleController.class);

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
}
