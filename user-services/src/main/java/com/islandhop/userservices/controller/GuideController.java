package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import com.islandhop.userservices.dto.GuideCertificateDTO;
import com.islandhop.userservices.dto.GuideProfileDTO;
import com.islandhop.userservices.model.GuideAccount;
import com.islandhop.userservices.model.GuideCertificate;
import com.islandhop.userservices.model.GuideLanguage;
import com.islandhop.userservices.model.GuideProfile;
import com.islandhop.userservices.repository.GuideAccountRepository;
import com.islandhop.userservices.repository.GuideProfileRepository;
import com.islandhop.userservices.service.GuideService;
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
 * REST controller for managing guide user operations.
 * Handles endpoints under /guide.
 */
@RestController
@RequestMapping("/guide")
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
@RequiredArgsConstructor
public class GuideController {

    private static final Logger logger = LoggerFactory.getLogger(GuideController.class);

    private final GuideAccountRepository accountRepository;
    private final GuideProfileRepository profileRepository;
    private final GuideService guideService;

    /**
     * Registers a new guide using a Firebase ID token.
     */
    @PostMapping("/session-register")
    public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody) {
        logger.info("POST /guide/session-register called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        if (idToken == null || role == null || !"guide".equalsIgnoreCase(role)) {
            logger.warn("Invalid session-register request for guide: {}", requestBody);
            return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
        }

        String email = guideService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Invalid Firebase token during guide session-register");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        if (accountRepository.existsByEmail(email)) {
            logger.info("Guide already registered: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        // Create guide account and basic profile
        GuideAccount account = guideService.createGuideAccount(email);
        GuideProfile profile = guideService.createBasicGuideProfile(email);
        
        logger.info("Guide account and basic profile created for email: {}", email);
        return ResponseEntity.ok(Map.of(
            "account", account,
            "profile", profile,
            "message", "Guide account created successfully"
        ));
    }

    /**
     * Updates the guide profile with complete information.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        logger.info("PUT /guide/profile called with body: {}", requestBody);
        
        // Get email from session
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile update attempted for non-existent guide: {}", email);
            return ResponseEntity.badRequest().body("Guide account does not exist");
        }

        try {
            GuideProfile profile = guideService.updateGuideProfile(email, requestBody);
            logger.info("Guide profile updated for: {}", email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error updating guide profile for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Gets the guide profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        GuideProfileDTO profile = guideService.getGuideProfileDTO(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        logger.info("Guide profile retrieved for: {}", profile);
        return ResponseEntity.ok(profile);
    }

    /**
     * Authenticates a guide and starts a session.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
        logger.info("POST /guide/login called with body: {}", requestBody);
        String idToken = requestBody.get("idToken");

        if (idToken == null) {
            logger.warn("Guide login failed: missing idToken");
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        String email = guideService.getEmailFromIdToken(idToken);
        if (email == null) {
            logger.warn("Guide login failed: invalid Firebase token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
        }

        // Check if guide account exists
        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Guide login failed: account not found for {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guide account not found");
        }

        session.setAttribute("userEmail", email);
        session.setAttribute("isGuideAuthenticated", true);
        logger.info("Guide logged in: {}", email);

        // Get profile completion status
        GuideProfile profile = profileRepository.findByEmail(email);
        boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;

        return ResponseEntity.ok(Map.of(
            "message", "Login successful", 
            "email", email,
            "profileComplete", profileComplete
        ));
    }

    /**
     * Logs out the current guide.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        session.invalidate();
        logger.info("Guide logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /**
     * Validates the guide session.
     */
    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isGuideAuthenticated");
        String email = (String) session.getAttribute("userEmail");

        logger.info("GET /guide/session/validate called. Authenticated: {}, Email: {}", isAuthenticated, email);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null) {
            // Get profile completion status
            GuideProfile profile = profileRepository.findByEmail(email);
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
        logger.info("GET /guide/health called");
        return ResponseEntity.ok("OK");
    }

    // Certificate Management Endpoints

    /**
     * Gets all certificates for the authenticated guide.
     */
    @GetMapping("/certificates")
    public ResponseEntity<?> getCertificates(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        try {
            List<GuideCertificateDTO> certificates = guideService.getCertificates(email);
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            logger.error("Error retrieving certificates for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving certificates: " + e.getMessage());
        }
    }

    /**
     * Updates/replaces all certificates for the authenticated guide.
     */
    @PutMapping("/certificates")
    public ResponseEntity<?> updateCertificates(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        //logger.info("PUT /guide/certificates called with body: {}", requestBody);
        
        String email = (String) session.getAttribute("userEmail");
        logger.info("(put)Updating certificates for guide: {}", email);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Certificate update attempted for non-existent guide: {}", email);
            return ResponseEntity.badRequest().body("Guide account does not exist");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> certificatesData = (List<Map<String, Object>>) requestBody.get("certifications");
            
            if (certificatesData == null) {
                return ResponseEntity.badRequest().body("Missing certificates data");
            }

            List<GuideCertificateDTO> updatedCertificates = guideService.updateCertificates(email, certificatesData);
            logger.info("Certificates updated for: {}", email);
            return ResponseEntity.ok(updatedCertificates);
        } catch (Exception e) {
            logger.error("Error updating certificates for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating certificates: " + e.getMessage());
        }
    }

    /**
     * Adds a single certificate for the authenticated guide.
     */
    @PostMapping("/certificates")
public ResponseEntity<?> addCertificate(@RequestBody Map<String, Object> requestBody, HttpSession session) {
    String email = (String) session.getAttribute("userEmail");
    logger.info("(post)Adding certificate for guide: {}", email);
    
    if (email == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
    }

    if (!accountRepository.existsByEmail(email)) {
        logger.warn("Certificate addition attempted for non-existent guide: {}", email);
        return ResponseEntity.badRequest().body("Guide account does not exist");
    }

    try {
        GuideCertificate certificate = guideService.saveCertificate(email, requestBody);
        GuideCertificateDTO certificateDTO = GuideCertificateDTO.fromEntity(certificate);
        logger.info("Certificate added for: {}", email);
        return ResponseEntity.ok(certificateDTO);
    } catch (Exception e) {
        logger.error("Full exception stack trace for {}: ", email, e); // This will show the full stack trace
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error adding certificate: " + e.getMessage());
    }
}
    // Language Management Endpoints

    /**
     * Gets all languages for the authenticated guide.
     */
    @GetMapping("/languages")
    public ResponseEntity<?> getLanguages(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        try {
            List<GuideLanguage> languages = guideService.getLanguages(email);
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            logger.error("Error retrieving languages for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving languages: " + e.getMessage());
        }
    }

    /**
     * Updates/replaces all languages for the authenticated guide.
     */
    @PutMapping("/languages")
    public ResponseEntity<?> updateLanguages(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        logger.info("PUT /guide/languages called with body: {}", requestBody);
        
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Language update attempted for non-existent guide: {}", email);
            return ResponseEntity.badRequest().body("Guide account does not exist");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> languagesData = (List<Map<String, Object>>) requestBody.get("languages");
            
            if (languagesData == null) {
                return ResponseEntity.badRequest().body("Missing languages data");
            }

            List<GuideLanguage> updatedLanguages = guideService.updateLanguages(email, languagesData);
            logger.info("Languages updated for: {}", email);
            return ResponseEntity.ok(updatedLanguages);
        } catch (Exception e) {
            logger.error("Error updating languages for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating languages: " + e.getMessage());
        }
    }
}
