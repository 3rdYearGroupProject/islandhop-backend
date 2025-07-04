package com.islandhop.tripplanning.controller;

import com.islandhop.tripplanning.config.CorsConfigConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = CorsConfigConstants.ALLOWED_ORIGIN, allowCredentials = CorsConfigConstants.ALLOW_CREDENTIALS)
@Slf4j
public class CorsTestController {

    @GetMapping("/cors")
    public ResponseEntity<?> testCors() {
        log.info("GET /test/cors called - testing CORS functionality");
        return ResponseEntity.ok(Map.of(
            "message", "CORS is working!",
            "timestamp", System.currentTimeMillis(),
            "allowedOrigin", CorsConfigConstants.ALLOWED_ORIGIN
        ));
    }

    @PostMapping("/cors")
    public ResponseEntity<?> testCorsPost(@RequestBody(required = false) Map<String, Object> body) {
        log.info("POST /test/cors called - testing CORS functionality with POST");
        return ResponseEntity.ok(Map.of(
            "message", "CORS POST is working!",
            "timestamp", System.currentTimeMillis(),
            "receivedBody", body,
            "allowedOrigin", CorsConfigConstants.ALLOWED_ORIGIN
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("CORS Test Controller is running");
    }
}
