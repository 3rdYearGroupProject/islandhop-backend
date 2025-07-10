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
     * Gets the tourist profile.
     * 
     * @param email Optional email parameter, otherwise uses session
     * @param session HTTP session
     * @return ResponseEntity with profile info or error status
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam(required = false) String email, HttpSession session) {
        logger.info("GET /tourist/profile called with email parameter: {}", email);
        
        // Use email from parameter if provided, otherwise get from session
        String profileEmail = email != null ? email : (String) session.getAttribute("userEmail");
        
        if (profileEmail == null) {
            logger.warn("No email provided and no email in session for profile request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        TouristProfile profile = profileRepository.findByEmail(profileEmail);
        if (profile == null) {
            logger.warn("Profile not found for email: {}", profileEmail);
            return ResponseEntity.notFound().build();
        }

        logger.info("Tourist profile retrieved for: {}", profileEmail);
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the tourist profile with new information.
     * 
     * @param requestBody Map containing profile fields to update
     * @param session HTTP session
     * @return ResponseEntity with updated profile info or error status
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        logger.info("PUT /tourist/profile called with body: {}", requestBody);
        
        // Get email from request body or session
        String email = (String) requestBody.get("email");
        if (email == null) {
            email = (String) session.getAttribute("userEmail");
        }
        
        if (email == null) {
            logger.warn("No email provided for profile update");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Profile update attempted for non-existent tourist: {}", email);
            return ResponseEntity.badRequest().body("Tourist account does not exist");
        }

        try {
            // Find existing profile
            TouristProfile profile = profileRepository.findByEmail(email);
            if (profile == null) {
                logger.warn("Profile not found for update: {}", email);
                return ResponseEntity.badRequest().body("Profile does not exist");
            }

            // Update profile fields
            if (requestBody.containsKey("firstName")) {
                profile.setFirstName((String) requestBody.get("firstName"));
            }
            if (requestBody.containsKey("lastName")) {
                profile.setLastName((String) requestBody.get("lastName"));
            }
            if (requestBody.containsKey("nationality")) {
                profile.setNationality((String) requestBody.get("nationality"));
            }
            if (requestBody.containsKey("languages")) {
                profile.setLanguages((List<String>) requestBody.get("languages"));
            }
            // Update profile picture if provided (byte array)
            if (requestBody.containsKey("profilePicture")) {
                Object picObj = requestBody.get("profilePicture");
                logger.info("Profile picture object type: {}", picObj != null ? picObj.getClass().getName() : "null");
                
                if (picObj instanceof List<?>) {
                    List<?> picList = (List<?>) picObj;
                    logger.info("Profile picture list size: {}", picList.size());
                    
                    if (picList.isEmpty()) {
                        profile.setProfilePic(null);
                        logger.info("Empty profile picture list, setting to null");
                    } else {
                        byte[] picBytes = new byte[picList.size()];
                        for (int i = 0; i < picList.size(); i++) {
                            Object val = picList.get(i);
                            if (val instanceof Number) {
                                int intVal = ((Number) val).intValue();
                                // Ensure value is in valid byte range (-128 to 127)
                                picBytes[i] = (byte) (intVal & 0xFF);
                            } else {
                                logger.warn("Non-numeric value at index {}: {}", i, val);
                                picBytes[i] = 0;
                            }
                        }
                        
                        profile.setProfilePic(picBytes);
                        logger.info("Profile picture byte array set, length: {}", picBytes.length);
                    }
                } else if (picObj == null) {
                    profile.setProfilePic(null);
                    logger.info("Profile picture set to null");
                } else {
                    logger.warn("Unexpected profile picture object type: {}, converting to string and ignoring", picObj.getClass().getName());
                    // Don't update profile picture if it's not in expected format
                }
            }
            // Update profile completion status if all required fields are present
            if (profile.getFirstName() != null && profile.getLastName() != null && 
                profile.getNationality() != null && profile.getLanguages() != null) {
                profile.setProfileCompletion(1);
            }

            TouristProfile updatedProfile = profileRepository.save(profile);
            logger.info("Tourist profile updated for: {}", email);
            return ResponseEntity.ok(updatedProfile);
            
        } catch (Exception e) {
            logger.error("Error updating tourist profile for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating profile: " + e.getMessage());
        }
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

        // Check if tourist account exists
        if (!accountRepository.existsByEmail(email)) {
            logger.warn("Tourist login failed: account not found for {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tourist account not found");
        }

        // Set session attributes
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", "tourist");
        session.setAttribute("isAuthenticated", true);
        logger.info("Tourist logged in: {}", email);

        // Get profile completion status
        TouristProfile profile = profileRepository.findByEmail(email);
        boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;

        return ResponseEntity.ok(Map.of(
            "message", "Login successful", 
            "email", email,
            "profileComplete", profileComplete
        ));
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
        String role = (String) session.getAttribute("userRole");

        logger.info("GET /tourist/session/validate called. Authenticated: {}, Email: {}, Role: {}", isAuthenticated, email, role);

        if (Boolean.TRUE.equals(isAuthenticated) && email != null && "tourist".equals(role)) {
            // Get profile completion status
            TouristProfile profile = profileRepository.findByEmail(email);
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
}