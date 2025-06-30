package com.islandhop.pooling.controller;

import com.islandhop.pooling.dto.PoolingRequest;
import com.islandhop.pooling.dto.PoolSuggestion;
import com.islandhop.pooling.model.TripPool;
import com.islandhop.pooling.service.PoolingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pooling")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PoolingController {

    private static final Logger logger = LoggerFactory.getLogger(PoolingController.class);

    private final PoolingService poolingService;

    public PoolingController(PoolingService poolingService) {
        this.poolingService = poolingService;
    }

    /**
     * Find potential pools for a user's trip
     * POST /pooling/find-matches
     */
    @PostMapping("/find-matches")
    public ResponseEntity<?> findPotentialPools(@RequestBody PoolingRequest request) {
        logger.info("POST /pooling/find-matches called for user: {} with trip: {}", 
                   request.getUserId(), request.getTripId());
        
        try {
            List<PoolSuggestion> suggestions = poolingService.findPotentialPools(request);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("Error finding potential pools: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to find potential pools", "message", e.getMessage()));
        }
    }

    /**
     * Create a new trip pool
     * POST /pooling/create-pool
     */
    @PostMapping("/create-pool")
    public ResponseEntity<?> createTripPool(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String tripId = request.get("tripId");
        String poolName = request.get("poolName");
        String description = request.get("description");
        
        logger.info("POST /pooling/create-pool called for user: {} with trip: {}", userId, tripId);
        
        try {
            TripPool pool = poolingService.createTripPool(userId, tripId, poolName, description);
            return ResponseEntity.ok(pool);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for pool creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating trip pool: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create pool", "message", e.getMessage()));
        }
    }

    /**
     * Join an existing trip pool
     * POST /pooling/join-pool/{poolId}
     */
    @PostMapping("/join-pool/{poolId}")
    public ResponseEntity<?> joinTripPool(@PathVariable String poolId, 
                                         @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String tripId = request.get("tripId");
        
        logger.info("POST /pooling/join-pool/{} called for user: {} with trip: {}", poolId, userId, tripId);
        
        try {
            TripPool pool = poolingService.joinTripPool(poolId, userId, tripId);
            return ResponseEntity.ok(pool);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for joining pool: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for joining pool: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error joining trip pool: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to join pool", "message", e.getMessage()));
        }
    }

    /**
     * Get all pools for a user
     * GET /pooling/my-pools/{userId}
     */
    @GetMapping("/my-pools/{userId}")
    public ResponseEntity<?> getUserPools(@PathVariable String userId) {
        logger.info("GET /pooling/my-pools/{} called", userId);
        
        try {
            List<TripPool> pools = poolingService.getUserPools(userId);
            return ResponseEntity.ok(pools);
        } catch (Exception e) {
            logger.error("Error fetching user pools: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch pools", "message", e.getMessage()));
        }
    }

    /**
     * Leave a trip pool
     * POST /pooling/leave-pool/{poolId}
     */
    @PostMapping("/leave-pool/{poolId}")
    public ResponseEntity<?> leaveTripPool(@PathVariable String poolId,
                                          @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        
        logger.info("POST /pooling/leave-pool/{} called for user: {}", poolId, userId);
        
        try {
            poolingService.leaveTripPool(poolId, userId);
            return ResponseEntity.ok(Map.of("message", "Successfully left the pool"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for leaving pool: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for leaving pool: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error leaving trip pool: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to leave pool", "message", e.getMessage()));
        }
    }

    /**
     * Get pool details by ID
     * GET /pooling/pool/{poolId}
     */
    @GetMapping("/pool/{poolId}")
    public ResponseEntity<?> getPoolDetails(@PathVariable String poolId) {
        logger.info("GET /pooling/pool/{} called", poolId);
        
        try {
            // Implementation would fetch pool details
            // For now, return a placeholder response
            return ResponseEntity.ok(Map.of("message", "Pool details endpoint - to be implemented"));
        } catch (Exception e) {
            logger.error("Error fetching pool details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch pool details", "message", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /pooling/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /pooling/health called");
        return ResponseEntity.ok("OK");
    }

    /**
     * Get service status and statistics
     * GET /pooling/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        logger.info("GET /pooling/status called");
        
        try {
            // Basic service status information
            Map<String, Object> status = Map.of(
                "service", "pooling-service",
                "status", "running",
                "timestamp", System.currentTimeMillis(),
                "version", "1.0.0"
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting service status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get status", "message", e.getMessage()));
        }
    }
}
