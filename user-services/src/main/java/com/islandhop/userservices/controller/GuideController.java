// package com.islandhop.userservices.controller;
//
// import com.islandhop.userservices.model.GuideAccount;
// import com.islandhop.userservices.model.GuideProfile;
// import com.islandhop.userservices.repository.GuideAccountRepository;
// import com.islandhop.userservices.repository.GuideProfileRepository;
// import com.islandhop.userservices.service.GuideService;
// import lombok.RequiredArgsConstructor;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import jakarta.servlet.http.HttpSession;
//
// import java.util.List;
// import java.util.Map;
//
// /**
//  * REST controller for managing guide user operations.
//  * Handles endpoints under /guide.
//  */
// @RestController
// @RequestMapping("/guide")
// @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
// @RequiredArgsConstructor
// public class GuideController {
//
//     private static final Logger logger = LoggerFactory.getLogger(GuideController.class);
//
//     private final GuideAccountRepository accountRepository;
//     private final GuideProfileRepository profileRepository;
//     private final GuideService guideService;
//
//     /**
//      * Registers a new guide using a Firebase ID token.
//      */
//     @PostMapping("/session-register")
//     public ResponseEntity<?> sessionRegister(@RequestBody Map<String, String> requestBody) {
//         logger.info("POST /guide/session-register called with body: {}", requestBody);
//         String idToken = requestBody.get("idToken");
//         String role = requestBody.get("role");
//
//         if (idToken == null || role == null || !"guide".equalsIgnoreCase(role)) {
//             logger.warn("Invalid session-register request for guide: {}", requestBody);
//             return ResponseEntity.badRequest().body("Missing or invalid idToken/role");
//         }
//
//         String email = guideService.getEmailFromIdToken(idToken);
//         if (email == null) {
//             logger.warn("Invalid Firebase token during guide session-register");
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
//         }
//
//         if (accountRepository.existsByEmail(email)) {
//             logger.info("Guide already registered: {}", email);
//             return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
//         }
//
//         GuideAccount account = guideService.createGuideAccount(email);
//         logger.info("Guide account created for email: {}", email);
//         return ResponseEntity.ok(account);
//     }
//
//     /**
//      * Completes the guide's profile.
//      */
//     @PostMapping("/complete-profile")
//     public ResponseEntity<?> completeProfile(@RequestBody Map<String, Object> requestBody) {
//         logger.info("POST /guide/complete-profile called with body: {}", requestBody);
//         String email = (String) requestBody.get("email");
//         String firstName = (String) requestBody.get("firstName");
//         String lastName = (String) requestBody.get("lastName");
//         String licenseId = (String) requestBody.get("licenseId");
//         List<String> languages = (List<String>) requestBody.get("languages");
//
//         if (!accountRepository.existsByEmail(email)) {
//             logger.warn("Profile completion attempted for non-existent guide: {}", email);
//             return ResponseEntity.badRequest().body("Guide account does not exist");
//         }
//
//         if (profileRepository.existsByEmail(email)) {
//             logger.info("Guide profile already exists: {}", email);
//             return ResponseEntity.status(HttpStatus.CONFLICT).body("Profile already completed");
//         }
//
//         GuideProfile profile = guideService.completeGuideProfile(email, firstName, lastName, licenseId, languages);
//         logger.info("Guide profile completed: {}", email);
//         return ResponseEntity.ok(profile);
//     }
//
//     /**
//      * Authenticates a guide and starts a session.
//      */
//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpSession session) {
//         logger.info("POST /guide/login called with body: {}", requestBody);
//         String idToken = requestBody.get("idToken");
//
//         if (idToken == null) {
//             logger.warn("Guide login failed: missing idToken");
//             return ResponseEntity.badRequest().body("Missing idToken");
//         }
//
//         String email = guideService.getEmailFromIdToken(idToken);
//         if (email == null) {
//             logger.warn("Guide login failed: invalid Firebase token");
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Firebase token");
//         }
//
//         session.setAttribute("guideEmail", email);
//         session.setAttribute("isGuideAuthenticated", true);
//         logger.info("Guide logged in: {}", email);
//
//         return ResponseEntity.ok(Map.of("message", "Login successful", "email", email));
//     }
//
//     /**
//      * Logs out the current guide.
//      */
//     @PostMapping("/logout")
//     public ResponseEntity<?> logout(HttpSession session) {
//         String email = (String) session.getAttribute("guideEmail");
//         session.invalidate();
//         logger.info("Guide logged out: {}", email);
//         return ResponseEntity.ok(Map.of("message", "Logged out"));
//     }
//
//     /**
//      * Validates the guide session.
//      */
//     @GetMapping("/session/validate")
//     public ResponseEntity<?> validateSession(HttpSession session) {
//         Boolean isAuthenticated = (Boolean) session.getAttribute("isGuideAuthenticated");
//         String email = (String) session.getAttribute("guideEmail");
//
//         logger.info("GET /guide/session/validate called. Authenticated: {}, Email: {}", isAuthenticated, email);
//
//         if (Boolean.TRUE.equals(isAuthenticated) && email != null) {
//             return ResponseEntity.ok(Map.of("valid", true, "email", email));
//         } else {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
//         }
//     }
//
//     /**
//      * Health check endpoint.
//      */
//     @GetMapping("/health")
//     public ResponseEntity<String> health() {
//         logger.info("GET /guide/health called");
//         return ResponseEntity.ok("OK");
//     }
// }
