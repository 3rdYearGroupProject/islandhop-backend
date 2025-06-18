package com.islandhop.userservices.controller;

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

@RestController
@RequestMapping("/tourist")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class TouristController {

    private static final Logger logger = LoggerFactory.getLogger(TouristController.class);

    private final TouristAccountRepository accountRepository;
    private final TouristProfileRepository profileRepository;
    private final TouristService touristService;

    @PostMapping("/session-register")
    public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody) {
        logger.info("POST /tourist/session-register called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        if (idToken == null || role == null || !"tourist".equalsIgnoreCase(role)) {
            logger.warn("Invalid session-register request: missing idToken or role");
            return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
        }

        String email = touristService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during session-register");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        if (accountRepository.existsByEmail(email)) {
            logger.info("Attempt to register already existing email: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        TouristAccount account = touristService.createTouristAccount(email);
        logger.info("Tourist account created for email: {}", email);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody Map<String, Object> requestBody) {
        logger.info("POST /tourist/complete-profile called with body: {}", requestBody);
        String email = (String) requestBody.get("email");
        String firstName = (String) requestBody.get("firstName");
        String lastName = (String) requestBody.get("lastName");
        String nationality = (String) requestBody.get("nationality");
        List<String> languages = (List<String>) requestBody.get("languages");

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile completion attempted for non-existent account: {}", email);
            return ResponseEntity.badRequest().body("Account does not exist");
        }

        if (profileRepository.existsByEmail(email)) {
            logger.info("Profile already completed for email: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Profile already completed");
        }

        TouristProfile profile = touristService.completeTouristProfile(email, firstName, lastName, nationality, languages);
        logger.info("Tourist profile completed for email: {}", email);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /tourist/login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        if (idToken == null) {
            logger.warn("Login attempt with missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        String email = touristService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Login failed: invalid Firebase token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        session.setAttribute("userEmail", email);
        session.setAttribute("isAuthenticated", true);
        logger.info("User logged in and session started: {}", email);

        return ResponseEntity.ok(Map.of("message", "Login successful", "email", email));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        session.invalidate();
        logger.info("User logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

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

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /tourist/health called");
        return ResponseEntity.ok("OK");
    }
}