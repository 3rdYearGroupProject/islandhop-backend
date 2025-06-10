package com.islandhop.userservices.controller;

import com.islandhop.userservices.dto.TouristRegistrationRequest;
import com.islandhop.userservices.model.Tourist;
import com.islandhop.userservices.service.TouristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tourists")
@RequiredArgsConstructor
public class TouristController {

    private final TouristService touristService;

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

    // Health check endpoint to test the backend
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}