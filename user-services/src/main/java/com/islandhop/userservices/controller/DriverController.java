package com.islandhop.userservices.controller;

import com.islandhop.userservices.config.CorsConfig;
import com.islandhop.userservices.model.DriverAccount;
import com.islandhop.userservices.model.DriverProfile;
import com.islandhop.userservices.model.DriverVehicle;
import com.islandhop.userservices.repository.DriverAccountRepository;
import com.islandhop.userservices.repository.DriverProfileRepository;
import com.islandhop.userservices.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST controller for managing driver user operations.
 * Handles endpoints under /driver.
 */
@RestController
@RequestMapping("/driver")
@CrossOrigin(origins = CorsConfig.ALLOWED_ORIGIN, allowCredentials = CorsConfig.ALLOW_CREDENTIALS)
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

        // Create driver account and basic profile
        DriverAccount account = driverService.createDriverAccount(email);
        DriverProfile profile = driverService.createBasicDriverProfile(email);
        
        logger.info("Driver account and basic profile created for email: {}", email);
        return ResponseEntity.ok(Map.of(
            "account", account,
            "profile", profile,
            "message", "Driver account created successfully"
        ));
    }

    /**
     * Updates the driver profile with complete information.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        logger.info("PUT /driver/profile called with body: {}", requestBody);
        
        // Get email from request body or session
        String email = (String) requestBody.get("email");
        if (email == null) {
            email = (String) session.getAttribute("userEmail");
        }
        
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile update attempted for non-existent driver: {}", email);
            return ResponseEntity.badRequest().body("Driver account does not exist");
        }

        try {
            DriverProfile profile = driverService.updateDriverProfile(email, requestBody);
            logger.info("Driver profile updated for: {}", email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error updating driver profile for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Gets the driver profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam(required = false) String email, HttpSession session) {
        // Use email from parameter if provided, otherwise get from session
        String profileEmail = email != null ? email : (String) session.getAttribute("userEmail");
        
        if (profileEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        DriverProfile profile = profileRepository.findByEmail(profileEmail);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

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

        // Check if driver account exists
        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Driver login failed: account not found for {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Driver account not found");
        }

        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", "driver");
        session.setAttribute("isAuthenticated", true);
        logger.info("Driver logged in: {}", email);

        // Get profile completion status
        DriverProfile profile = profileRepository.findByEmail(email);
        boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;

        return ResponseEntity.ok(Map.of(
            "message", "Login successful", 
            "email", email,
            "profileComplete", profileComplete
        ));
    }

    /**
     * Logs out the current driver.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        session.invalidate();
        logger.info("Driver logged out: {}", email);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /**
     * Validates the driver session.
     */
    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        logger.info("GET /driver/session/validate called. Authenticated: {}, Email: {}, Role: {}", isAuthenticated, email, role);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null && "driver".equals(role)) {
            // Get profile completion status
            DriverProfile profile = profileRepository.findByEmail(email);
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
        logger.info("GET /driver/health called");
        return ResponseEntity.ok("OK");
    }

    /**
     * Get driver vehicle information with images as base64.
     */
    @GetMapping("/vehicle")
    public ResponseEntity<?> getDriverVehicle(@RequestParam(required = false) String email, HttpSession session) {
        logger.info("GET /driver/vehicle called with email parameter: {}", email);
        
        // Use email from parameter if provided, otherwise get from session
        String driverEmail = email != null ? email : (String) session.getAttribute("userEmail");
        if (driverEmail == null) {
            logger.warn("No email provided and no email in session for vehicle request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        try {
            Map<String, Object> vehicleData = driverService.getDriverVehicleWithImages(driverEmail);
            if (vehicleData == null) {
                // Create basic vehicle record if it doesn't exist
                driverService.createBasicDriverVehicle(driverEmail);
                vehicleData = driverService.getDriverVehicleWithImages(driverEmail);
            }
            return ResponseEntity.ok(vehicleData);
        } catch (Exception e) {
            logger.error("Error getting driver vehicle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving vehicle data");
        }
    }

    /**
     * Update driver vehicle information with multipart form data.
     */
    @PutMapping("/vehicle")
    public ResponseEntity<?> updateDriverVehicle(
            HttpSession session,
            @RequestParam Map<String, Object> requestBody,
            @RequestParam Map<String, MultipartFile> files) {
        logger.info("PUT /driver/vehicle called with {} fields and {} files", 
                   requestBody.size(), files.size());
        
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            logger.warn("No email in session for vehicle update");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        try {
            driverService.updateDriverVehicle(email, requestBody, files);
            Map<String, Object> response = driverService.getDriverVehicleWithImages(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating driver vehicle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating vehicle data");
        }
    }

    /**
     * Upload driver's driving license document.
     */
    @PostMapping("/uploadDrivingLicense")
    public ResponseEntity<?> uploadDrivingLicense(
            @RequestParam("drivingLicense") MultipartFile file,
            HttpSession session) {
        logger.info("POST /driver/uploadDrivingLicense called with file: {}",
                   file != null ? file.getOriginalFilename() : "null");

        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            logger.warn("No email in session for driving license upload");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (file == null || file.isEmpty()) {
            logger.warn("Empty or null file provided for driving license upload");
            return ResponseEntity.badRequest().body("File is required");
        }

        try {
            driverService.uploadDrivingLicense(email, file);
            logger.info("Driving license uploaded successfully for: {}", email);
            return ResponseEntity.ok(Map.of("message", "Driving license uploaded successfully"));
        } catch (Exception e) {
            logger.error("Error uploading driving license for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error uploading driving license: " + e.getMessage());
        }
    }

    /**
     * Upload driver's SLTDA license document.
     */
    @PostMapping("/uploadSltdaLicense")
    public ResponseEntity<?> uploadSltdaLicense(
            @RequestParam("sltdaLicense") MultipartFile file,
            HttpSession session) {
        logger.info("POST /driver/uploadSltdaLicense called with file: {}",
                   file != null ? file.getOriginalFilename() : "null");

        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            logger.warn("No email in session for SLTDA license upload");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (file == null || file.isEmpty()) {
            logger.warn("Empty or null file provided for SLTDA license upload");
            return ResponseEntity.badRequest().body("File is required");
        }

        try {
            driverService.uploadSltdaLicense(email, file);
            logger.info("SLTDA license uploaded successfully for: {}", email);
            return ResponseEntity.ok(Map.of("message", "SLTDA license uploaded successfully"));
        } catch (Exception e) {
            logger.error("Error uploading SLTDA license for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error uploading SLTDA license: " + e.getMessage());
        }
    }

}
