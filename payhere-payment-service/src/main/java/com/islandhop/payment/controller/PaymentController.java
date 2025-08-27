package com.islandhop.payment.controller;

import com.islandhop.payment.dto.PaymentRequest;
import com.islandhop.payment.dto.PaymentResponse;
import com.islandhop.payment.dto.PayHereNotification;
import com.islandhop.payment.service.PaymentService;
import com.islandhop.payment.service.TripPaymentService;
import com.islandhop.payment.model.PaymentDetails;
import com.islandhop.payment.model.PaidTrip;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final TripPaymentService tripPaymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService, TripPaymentService tripPaymentService) {
        this.paymentService = paymentService;
        this.tripPaymentService = tripPaymentService;
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
     * Handle PayHere payment notification via form parameters (actual PayHere callback)
     * @param params Form parameters from PayHere
     * @param httpRequest HTTP request for logging
     * @return PaymentResponse with notification processing result
     */
    @PostMapping("/notify-form")
    public ResponseEntity<String> handlePayHereNotificationForm(
            @RequestParam Map<String, String> params,
            HttpServletRequest httpRequest) {
        
        logger.info("Received PayHere form notification from IP: {} for order: {}", 
                   getClientIpAddress(httpRequest), params.get("order_id"));
        logger.debug("Form notification details: {}", params);
        
        String orderId = params.get("order_id");
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.error("Invalid form notification - missing order ID");
            return ResponseEntity.badRequest().body("Order ID is required");
        }
        
        try {
            // Create PayHereNotification object from form parameters
            PayHereNotification notification = new PayHereNotification();
            notification.setOrderId(orderId);
            notification.setStatusCode(Integer.parseInt(params.get("status_code")));
            notification.setStatusMessage(params.get("status_message"));
            notification.setPaymentId(params.get("payment_id"));
            notification.setAmount(new BigDecimal(params.get("amount")));
            notification.setCurrency(params.get("currency"));
            notification.setMerchantId(params.get("merchant_id"));
            notification.setPayHereAmount(new BigDecimal(params.get("payhere_amount")));
            notification.setPayHereCurrency(params.get("payhere_currency"));
            notification.setMd5Signature(params.get("md5sig"));
            
            PaymentResponse response = paymentService.handlePayHereNotification(notification);
            
            if ("success".equals(response.getStatus())) {
                logger.info("PayHere form notification processed successfully for order: {}", orderId);
                return ResponseEntity.ok("Payment verified");
            } else if ("pending".equals(response.getStatus())) {
                logger.info("Payment is pending for order: {}", orderId);
                return ResponseEntity.ok("Payment pending");
            } else {
                logger.error("Payment failed for order: {} - {}", orderId, response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error processing PayHere form notification for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
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
     * Get paid trip details by trip ID
     * @param tripId Trip ID
     * @param httpRequest HTTP request for logging
     * @return Paid trip details
     */
    @GetMapping("/paid-trip/{tripId}")
    public ResponseEntity<Map<String, Object>> getPaidTrip(
            @PathVariable String tripId,
            HttpServletRequest httpRequest) {
        
        logger.info("Paid trip details requested from IP: {} for trip: {}", 
                   getClientIpAddress(httpRequest), tripId);
        
        if (tripId == null || tripId.trim().isEmpty()) {
            logger.error("Invalid request - missing trip ID");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Trip ID is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Optional<PaidTrip> paidTripOpt = tripPaymentService.getPaidTripById(tripId);
            
            if (paidTripOpt.isEmpty()) {
                logger.warn("Paid trip not found for ID: {}", tripId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Paid trip not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", paidTripOpt.get());
            
            logger.info("Paid trip details retrieved for ID: {}", tripId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error retrieving paid trip for ID: {}", tripId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get payment details by order ID
     * @param orderId Order ID
     * @param httpRequest HTTP request for logging
     * @return Payment details
     */
    @GetMapping("/payment-details/{orderId}")
    public ResponseEntity<Map<String, Object>> getPaymentDetails(
            @PathVariable String orderId,
            HttpServletRequest httpRequest) {
        
        logger.info("Payment details requested from IP: {} for order: {}", 
                   getClientIpAddress(httpRequest), orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.error("Invalid request - missing order ID");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Order ID is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Optional<PaymentDetails> paymentDetailsOpt = tripPaymentService.getPaymentDetailsByOrderId(orderId);
            
            if (paymentDetailsOpt.isEmpty()) {
                logger.warn("Payment details not found for order: {}", orderId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Payment details not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", paymentDetailsOpt.get());
            
            logger.info("Payment details retrieved for order: {}", orderId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error retrieving payment details for order: {}", orderId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Check if payment exists in database by order ID
     * @param orderId Order ID
     * @param httpRequest HTTP request for logging
     * @return Payment existence status
     */
    @GetMapping("/check-payment/{orderId}")
    public ResponseEntity<Map<String, Object>> checkPaymentExists(
            @PathVariable String orderId,
            HttpServletRequest httpRequest) {
        
        logger.info("Payment existence check requested from IP: {} for order: {}", 
                   getClientIpAddress(httpRequest), orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.error("Invalid request - missing order ID");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Order ID is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Optional<PaymentDetails> paymentDetailsOpt = tripPaymentService.getPaymentDetailsByOrderId(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("exists", paymentDetailsOpt.isPresent());
            
            if (paymentDetailsOpt.isPresent()) {
                PaymentDetails payment = paymentDetailsOpt.get();
                response.put("paymentStatus", payment.getPaymentStatus());
                response.put("amount", payment.getAmount());
                response.put("currency", payment.getCurrency());
                response.put("createdAt", payment.getCreatedAt());
                response.put("paymentDate", payment.getPaymentDate());
            }
            
            logger.info("Payment existence check completed for order: {} - Exists: {}", 
                       orderId, paymentDetailsOpt.isPresent());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error checking payment existence for order: {}", orderId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Internal server error: " + e.getMessage());
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
