package com.islandhop.adminservice.controller;

import com.islandhop.adminservice.model.SystemStatusResponse;
import com.islandhop.adminservice.service.SystemStatusService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin operations.
 * Handles endpoints under /admin.
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final SystemStatusService systemStatusService;

    /**
     * Gets the status of all external services (Redis, Firebase, MongoDB).
     * 
     * @return ResponseEntity containing SystemStatusResponse with service statuses
     */
    @GetMapping("/status")
    public ResponseEntity<SystemStatusResponse> getSystemStatus() {
        logger.info("GET /admin/status called");
        
        try {
            SystemStatusResponse statusResponse = systemStatusService.getSystemStatus();
            logger.info("System status retrieved successfully: {}", statusResponse);
            return ResponseEntity.ok(statusResponse);
        } catch (Exception e) {
            logger.error("Error retrieving system status: {}", e.getMessage(), e);
            
            // Return a fallback response indicating all services are down
            SystemStatusResponse fallbackResponse = new SystemStatusResponse(
                SystemStatusResponse.Status.DOWN,
                SystemStatusResponse.Status.DOWN,
                SystemStatusResponse.Status.DOWN
            );
            return ResponseEntity.internalServerError().body(fallbackResponse);
        }
    }
}
