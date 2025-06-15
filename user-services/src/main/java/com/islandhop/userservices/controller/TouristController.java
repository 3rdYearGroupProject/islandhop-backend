package com.islandhop.userservices.controller;

import com.islandhop.userservices.dto.TouristRegistrationRequest;
import com.islandhop.userservices.model.Tourist;
import com.islandhop.userservices.service.TouristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/tourist")
@CrossOrigin(origins = "http://localhost:5173")

@RequiredArgsConstructor
public class TouristController {

    private final TouristService touristService;

    @PostMapping("/session-register")
    public ResponseEntity<Tourist> registerTouristWithSession(
            @RequestBody Map<String, String> requestBody) {
        String idToken = requestBody.get("idToken");
        String role = requestBody.get("role");

        if (idToken == null || role == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verify the ID token using Firebase Admin SDK
        String firebaseUid = touristService.verifyFirebaseIdToken(idToken);
        if (firebaseUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Create a TouristRegistrationRequest based on the role
        TouristRegistrationRequest registrationRequest = new TouristRegistrationRequest();
        registrationRequest.setName("Default Name"); // Replace with actual logic
        registrationRequest.setEmail("default@example.com"); // Replace with actual logic
        registrationRequest.setDateOfBirth(LocalDate.now()); // Replace with actual logic
        registrationRequest.setNationality("Default Nationality"); // Replace with actual logic
        registrationRequest.setLanguages(List.of("English")); // Replace with actual logic

        Tourist registeredTourist = touristService.registerTourist(firebaseUid, registrationRequest);
        return ResponseEntity.ok(registeredTourist);
    }

    @PostMapping("/register")
    public ResponseEntity<Tourist> registerTourist(
            @RequestHeader("X-Firebase-Auth") String firebaseUid,
            @Valid @RequestBody TouristRegistrationRequest request) {
        return ResponseEntity.ok(touristService.registerTourist(firebaseUid, request));
    }

    @GetMapping("/me")
    public ResponseEntity<Tourist> getCurrentTourist(
            @RequestHeader("X-Firebase-Auth") String firebaseUid) {
        return ResponseEntity.ok(touristService.getTouristByFirebaseUid(firebaseUid));
    }

    @PatchMapping("/update")
    public ResponseEntity<Tourist> updateTourist(
            @RequestHeader("X-Firebase-Auth") String firebaseUid,
            @Valid @RequestBody TouristRegistrationRequest request) {
        return ResponseEntity.ok(touristService.updateTourist(firebaseUid, request));
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivateTourist(
            @RequestHeader("X-Firebase-Auth") String firebaseUid) {
        touristService.deactivateTourist(firebaseUid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteTourist(
            @RequestHeader("X-Firebase-Auth") String firebaseUid) {
        touristService.deleteTourist(firebaseUid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify/send")
    public ResponseEntity<String> sendVerificationCode(
            @RequestHeader("X-Firebase-Auth") String firebaseUid) {
        String otp = touristService.generateAndSendOTP(firebaseUid);
        return ResponseEntity.ok(otp); // In production, don't return the OTP
    }

    @PostMapping("/verify/check")
    public ResponseEntity<Boolean> verifyOTP(
            @RequestHeader("X-Firebase-Auth") String firebaseUid,
            @RequestParam String otp) {
        boolean isValid = touristService.verifyOTP(firebaseUid, otp);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}