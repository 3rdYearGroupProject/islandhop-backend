package com.tourism.userservices.controller;

import com.tourism.userservices.dto.RegisterTouristRequest;
import com.tourism.userservices.dto.UpdateTouristRequest;
import com.tourism.userservices.dto.OtpRequest;
import com.tourism.userservices.dto.OtpVerifyRequest;
import com.tourism.userservices.entity.Tourist;
import com.tourism.userservices.service.TouristService;
import com.tourism.userservices.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/tourists")
public class TouristController {

    @Autowired
    private TouristService touristService;

    @PostMapping("/register")
    public ResponseEntity<String> registerTourist(@Valid @RequestBody RegisterTouristRequest request, 
                                                   @AuthenticationPrincipal String firebaseUid) {
        touristService.registerTourist(request, firebaseUid);
        return ResponseEntity.ok("Tourist registered successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<Tourist> getTouristProfile(@AuthenticationPrincipal String firebaseUid) {
        Tourist tourist = touristService.getTouristByFirebaseUid(firebaseUid);
        return ResponseEntity.ok(tourist);
    }

    @PatchMapping("/update")
    public ResponseEntity<Tourist> updateTourist(@Valid @RequestBody UpdateTouristRequest request, 
                                                  @AuthenticationPrincipal String firebaseUid) {
        Tourist updatedTourist = touristService.updateTourist(request, firebaseUid);
        return ResponseEntity.ok(updatedTourist);
    }

    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(@AuthenticationPrincipal String firebaseUid) {
        touristService.deactivateAccount(firebaseUid);
        return ResponseEntity.ok("Account deactivated. Verification email sent.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@Valid @RequestBody OtpVerifyRequest otpVerifyRequest, 
                                                 @AuthenticationPrincipal String firebaseUid) {
        touristService.deleteAccount(otpVerifyRequest, firebaseUid);
        return ResponseEntity.ok("Account deleted successfully.");
    }

    @PostMapping("/verify/send")
    public ResponseEntity<String> sendVerificationCode(@Valid @RequestBody OtpRequest otpRequest, 
                                                       @AuthenticationPrincipal String firebaseUid) {
        touristService.sendVerificationCode(firebaseUid);
        return ResponseEntity.ok("Verification code sent.");
    }

    @PostMapping("/verify/check")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpVerifyRequest otpVerifyRequest, 
                                             @AuthenticationPrincipal String firebaseUid) {
        touristService.verifyOtp(otpVerifyRequest, firebaseUid);
        return ResponseEntity.ok("OTP verified successfully.");
    }
}