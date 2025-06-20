package com.islandhop.userservices.controller;

import com.islandhop.userservices.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class UniversalLoginController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLoginController.class);
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        if (idToken == null) {
            logger.warn("Login attempt with missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        Map<String, Object> userDetails = userService.validateAndGetUserDetails(idToken);
        if (userDetails == null) {
            logger.warn("Login failed: invalid Firebase token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Store session attributes
        session.setAttribute("userEmail", userDetails.get("email"));
        session.setAttribute("userRole", userDetails.get("role"));
        session.setAttribute("isAuthenticated", true);

        logger.info("User logged in: {} as {}", userDetails.get("email"), userDetails.get("role"));
        return ResponseEntity.ok(userDetails);
    }
}
