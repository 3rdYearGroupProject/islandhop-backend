package com.islandhop.payment.controller;

import com.islandhop.payment.dto.PaymentRequest;
import com.islandhop.payment.dto.PaymentResponse;
import com.islandhop.payment.dto.PayHereNotification;
import com.islandhop.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PayHere Payment Controller
 * Handles all payment-related REST endpoints
 */
@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * Create PayHere payment
     * @param paymentRequest Payment request details
     * @param bindingResult Validation result
     * @param httpRequest HTTP request for logging
     * @return PaymentResponse with PayHere payment data
     */
    @PostMapping("/create-payhere-payment")
    public ResponseEntity<PaymentResponse> createPayHerePayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        logger.info("Received payment request from IP: {} for order: {}", 
                   getClientIpAddress(httpRequest), paymentRequest.getOrderId());
        logger.debug("Payment request details: {}", paymentRequest);
        
        // Validate request
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.error("Payment request validation failed for order: {} - Errors: {}", 
                        paymentRequest.getOrderId(), errorMessage);
            
            PaymentResponse errorResponse = PaymentResponse.error(
                paymentRequest.getOrderId(), 
                "Validation failed: " + errorMessage
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            PaymentResponse response = paymentService.createPayHerePayment(paymentRequest);
            
            if ("success".equals(response.getStatus())) {
                logger.info("PayHere payment created successfully for order: {}", paymentRequest.getOrderId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.error("Failed to create PayHere payment for order: {} - {}", 
                            paymentRequest.getOrderId(), response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error creating PayHere payment for order: {}", 
                        paymentRequest.getOrderId(), e);
            
            PaymentResponse errorResponse = PaymentResponse.error(
                paymentRequest.getOrderId(), 
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Handle PayHere payment notification
     * @param notification PayHere notification data
     * @param httpRequest HTTP request for logging
     * @return PaymentResponse with notification processing result
     */
    @PostMapping("/notify")
    public ResponseEntity<PaymentResponse> handlePayHereNotification(
            @RequestBody PayHereNotification notification,
            HttpServletRequest httpRequest) {
        
        logger.info("Received PayHere notification from IP: {} for order: {}", 
                   getClientIpAddress(httpRequest), notification.getOrderId());
        logger.debug("Notification details: {}", notification);
        
        // Basic validation
        if (notification.getOrderId() == null || notification.getOrderId().trim().isEmpty()) {
            logger.error("Invalid notification - missing order ID");
            return ResponseEntity.badRequest().body(
                PaymentResponse.error(null, "Order ID is required")
            );
        }
        
        try {
            PaymentResponse response = paymentService.handlePayHereNotification(notification);
            
            if ("success".equals(response.getStatus())) {
                logger.info("PayHere notification processed successfully for order: {}", 
                           notification.getOrderId());
                return ResponseEntity.ok(response);
            } else if ("pending".equals(response.getStatus())) {
                logger.info("Payment is pending for order: {}", notification.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                logger.error("Payment failed for order: {} - {}", 
                            notification.getOrderId(), response.getMessage());
                return ResponseEntity.ok(response); // Return 200 for PayHere compatibility
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error processing PayHere notification for order: {}", 
                        notification.getOrderId(), e);
            
            PaymentResponse errorResponse = PaymentResponse.error(
                notification.getOrderId(), 
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get payment status by order ID
     * @param orderId Order ID
     * @param httpRequest HTTP request for logging
     * @return PaymentResponse with current payment status
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable String orderId,
            HttpServletRequest httpRequest) {
        
        logger.info("Payment status requested from IP: {} for order: {}", 
                   getClientIpAddress(httpRequest), orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.error("Invalid request - missing order ID");
            return ResponseEntity.badRequest().body(
                PaymentResponse.error(null, "Order ID is required")
            );
        }
        
        try {
            PaymentResponse response = paymentService.getPaymentStatus(orderId);
            
            if ("error".equals(response.getStatus())) {
                logger.warn("Payment status not found for order: {}", orderId);
                return ResponseEntity.notFound().build();
            }
            
            logger.info("Payment status retrieved for order: {} - Status: {}", 
                       orderId, response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error retrieving payment status for order: {}", orderId, e);
            
            PaymentResponse errorResponse = PaymentResponse.error(
                orderId, 
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     * @return Service health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "PayHere Payment Service");
        healthStatus.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(healthStatus);
    }
    
    /**
     * Get client IP address from HTTP request
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Global exception handler for validation errors
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentResponse> handleGlobalException(Exception ex) {
        logger.error("Global exception handler - Unexpected error", ex);
        
        PaymentResponse errorResponse = PaymentResponse.error(
            null, 
            "Internal server error: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
