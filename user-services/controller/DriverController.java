package com.example.driver.controller;

import com.example.driver.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/driver")
public class DriverController {

    @Autowired
    private DriverService driverService;

    /**
     * Uploads the driver's license document only.
     */
    @PostMapping("/uploadDrivingLicense")
    public ResponseEntity<?> uploadDrivingLicense(HttpSession session,
                                                  @RequestParam("drivingLicense") MultipartFile drivingLicense) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        if (drivingLicense == null || drivingLicense.isEmpty()) {
            return ResponseEntity.badRequest().body("Driving license file is required");
        }
        try {
            driverService.uploadDrivingLicense(email, drivingLicense);
            return ResponseEntity.ok("Driving license uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading driving license");
        }
    }

    /**
     * Uploads the SLTDA license document only.
     */
    @PostMapping("/uploadSltdaLicense")
    public ResponseEntity<?> uploadSltdaLicense(HttpSession session,
                                                @RequestParam("sltdaLicense") MultipartFile sltdaLicense) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        if (sltdaLicense == null || sltdaLicense.isEmpty()) {
            return ResponseEntity.badRequest().body("SLTDA license file is required");
        }
        try {
            driverService.uploadSltdaLicense(email, sltdaLicense);
            return ResponseEntity.ok("SLTDA license uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading SLTDA license");
        }
    }
}
