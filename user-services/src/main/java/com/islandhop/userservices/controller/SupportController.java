package com.islandhop.userservices.controller;

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
public class SupportController {

    private static final Logger logger = LoggerFactory.getLogger(SupportController.class);

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
}