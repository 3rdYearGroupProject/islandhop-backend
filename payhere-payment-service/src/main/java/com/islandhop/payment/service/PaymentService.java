package com.islandhop.payment.service;

import com.islandhop.payment.dto.PaymentRequest;
import com.islandhop.payment.dto.PaymentResponse;
import com.islandhop.payment.dto.PayHereNotification;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PayHere payment service for handling payment operations
 */
@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Value("${payhere.merchant-id}")
    private String merchantId;
    
    @Value("${payhere.secret}")
    private String merchantSecret;
    
    @Value("${payhere.notify-url}")
    private String notifyUrl;
    
    @Value("${payhere.sandbox}")
    private boolean sandbox;
    
    // In-memory storage for payment status (In production, use database)
    private final Map<String, PaymentStatus> paymentStatusMap = new ConcurrentHashMap<>();
    
    /**
     * Create PayHere payment request
     * @param paymentRequest Payment request details
     * @return PaymentResponse with PayHere payment data
     */
    public PaymentResponse createPayHerePayment(PaymentRequest paymentRequest) {
        logger.info("Creating PayHere payment for order: {}", paymentRequest.getOrderId());
        
        try {
            // Generate PayHere payment data
            Map<String, Object> payHereData = generatePayHereData(paymentRequest);
            
            // Store payment status as pending
            paymentStatusMap.put(paymentRequest.getOrderId(), 
                new PaymentStatus(paymentRequest.getOrderId(), "PENDING", 
                    paymentRequest.getAmount(), paymentRequest.getCurrency(), LocalDateTime.now()));
            
            PaymentResponse response = PaymentResponse.success(
                paymentRequest.getOrderId(), 
                null, 
                paymentRequest.getAmount(), 
                paymentRequest.getCurrency()
            );
            response.setPayHereData(payHereData);
            response.setMessage("PayHere payment data generated successfully");
            
            logger.info("PayHere payment created successfully for order: {}", paymentRequest.getOrderId());
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating PayHere payment for order: {}", paymentRequest.getOrderId(), e);
            return PaymentResponse.error(paymentRequest.getOrderId(), "Failed to create PayHere payment: " + e.getMessage());
        }
    }
    
    /**
     * Handle PayHere payment notification
     * @param notification PayHere notification data
     * @return PaymentResponse with notification processing result
     */
    public PaymentResponse handlePayHereNotification(PayHereNotification notification) {
        logger.info("Processing PayHere notification for order: {}", notification.getOrderId());
        
        try {
            // Verify notification signature
            if (!verifyNotificationSignature(notification)) {
                logger.error("Invalid notification signature for order: {}", notification.getOrderId());
                return PaymentResponse.error(notification.getOrderId(), "Invalid notification signature");
            }
            
            // Update payment status based on notification
            String status = determinePaymentStatus(notification);
            paymentStatusMap.put(notification.getOrderId(), 
                new PaymentStatus(notification.getOrderId(), status, 
                    notification.getAmount(), notification.getCurrency(), LocalDateTime.now()));
            
            logger.info("Payment status updated to {} for order: {}", status, notification.getOrderId());
            
            if (notification.isPaymentSuccessful()) {
                return PaymentResponse.success(notification.getOrderId(), notification.getPaymentId(), 
                    notification.getAmount(), notification.getCurrency());
            } else if (notification.isPaymentPending()) {
                return PaymentResponse.pending(notification.getOrderId(), "Payment is pending");
            } else {
                return PaymentResponse.failure(notification.getOrderId(), notification.getStatusMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing PayHere notification for order: {}", notification.getOrderId(), e);
            return PaymentResponse.error(notification.getOrderId(), "Failed to process notification: " + e.getMessage());
        }
    }
    
    /**
     * Get payment status by order ID
     * @param orderId Order ID
     * @return PaymentResponse with current payment status
     */
    public PaymentResponse getPaymentStatus(String orderId) {
        logger.info("Getting payment status for order: {}", orderId);
        
        PaymentStatus status = paymentStatusMap.get(orderId);
        if (status == null) {
            logger.warn("Payment status not found for order: {}", orderId);
            return PaymentResponse.error(orderId, "Payment status not found");
        }
        
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setStatus(status.getStatus().toLowerCase());
        response.setAmount(status.getAmount());
        response.setCurrency(status.getCurrency());
        response.setTimestamp(status.getTimestamp());
        response.setMessage("Payment status retrieved successfully");
        
        logger.info("Payment status retrieved for order: {} - Status: {}", orderId, status.getStatus());
        return response;
    }
    
    /**
     * Generate PayHere payment data
     * @param paymentRequest Payment request
     * @return Map containing PayHere payment data
     */
    private Map<String, Object> generatePayHereData(PaymentRequest paymentRequest) {
        Map<String, Object> payHereData = new HashMap<>();
        
        // Basic payment data
        payHereData.put("merchant_id", merchantId);
        payHereData.put("return_url", "http://localhost:3000/payment/success");
        payHereData.put("cancel_url", "http://localhost:3000/payment/cancel");
        payHereData.put("notify_url", notifyUrl);
        
        // Order details
        payHereData.put("order_id", paymentRequest.getOrderId());
        payHereData.put("items", paymentRequest.getItemName());
        payHereData.put("currency", paymentRequest.getCurrency());
        payHereData.put("amount", String.format("%.2f", paymentRequest.getAmount()));
        
        // Customer details
        PaymentRequest.CustomerDetails customer = paymentRequest.getCustomerDetails();
        payHereData.put("first_name", customer.getFirstName());
        payHereData.put("last_name", customer.getLastName());
        payHereData.put("email", customer.getEmail());
        payHereData.put("phone", customer.getPhone());
        payHereData.put("address", customer.getAddress());
        payHereData.put("city", customer.getCity());
        payHereData.put("country", customer.getCountry());
        
        // Generate hash
        String hash = generatePayHereHash(paymentRequest);
        payHereData.put("hash", hash);
        
        // Sandbox mode
        if (sandbox) {
            payHereData.put("sandbox", true);
        }
        
        return payHereData;
    }
    
    /**
     * Generate PayHere payment hash
     * @param paymentRequest Payment request
     * @return Generated hash
     */
    private String generatePayHereHash(PaymentRequest paymentRequest) {
        // PayHere hash procedure:
        // Step 1: Hash the merchant secret first
        String hashedMerchantSecret = DigestUtils.md5Hex(merchantSecret.getBytes()).toUpperCase();
        
        // Step 2: Format amount with 2 decimal places
        String formattedAmount = String.format("%.2f", paymentRequest.getAmount());
        
        // Step 3: Create hash string: merchantId + orderId + amount + currency + hashedMerchantSecret
        String hashString = merchantId + 
                           paymentRequest.getOrderId() + 
                           formattedAmount + 
                           paymentRequest.getCurrency() + 
                           hashedMerchantSecret;
        
        logger.debug("Merchant ID: {}", merchantId);
        logger.debug("Merchant Secret (first 10 chars): {}...", merchantSecret.substring(0, Math.min(10, merchantSecret.length())));
        logger.debug("Hashed Merchant Secret: {}", hashedMerchantSecret);
        logger.debug("Hash string for order {}: {}", paymentRequest.getOrderId(), 
                    hashString.replace(hashedMerchantSecret, "***HASHED_SECRET***"));
        
        // Step 4: Generate final hash
        String hash = DigestUtils.md5Hex(hashString.getBytes()).toUpperCase();
        logger.debug("Generated hash for order {}: {}", paymentRequest.getOrderId(), hash);
        
        return hash;
    }
    
    /**
     * Verify PayHere notification signature
     * @param notification PayHere notification
     * @return true if signature is valid
     */
    private boolean verifyNotificationSignature(PayHereNotification notification) {
        // PayHere notification verification procedure:
        // Step 1: Hash the merchant secret first
        String hashedMerchantSecret = DigestUtils.md5Hex(merchantSecret.getBytes()).toUpperCase();
        
        // Step 2: Create hash string: merchantId + orderId + amount + currency + statusCode + hashedMerchantSecret
        String localHashString = notification.getMerchantId() + 
                                notification.getOrderId() + 
                                notification.getPayHereAmount().toString() + 
                                notification.getPayHereCurrency() + 
                                notification.getStatusCode() + 
                                hashedMerchantSecret;
        
        logger.debug("Notification hash string for order {}: {}", notification.getOrderId(), 
                    localHashString.replace(hashedMerchantSecret, "***HASHED_SECRET***"));
        
        // Step 3: Generate local hash
        String localHash = DigestUtils.md5Hex(localHashString.getBytes()).toUpperCase();
        logger.debug("Local hash: {}, PayHere hash: {}", localHash, notification.getMd5Signature());
        
        boolean isValid = localHash.equals(notification.getMd5Signature());
        logger.debug("Signature verification for order {}: {}", notification.getOrderId(), 
                    isValid ? "VALID" : "INVALID");
        
        return isValid;
    }
    
    /**
     * Determine payment status from notification
     * @param notification PayHere notification
     * @return Payment status string
     */
    private String determinePaymentStatus(PayHereNotification notification) {
        if (notification.isPaymentSuccessful()) {
            return "SUCCESS";
        } else if (notification.isPaymentPending()) {
            return "PENDING";
        } else {
            return "FAILED";
        }
    }
    
    /**
     * Payment status inner class
     */
    private static class PaymentStatus {
        private final String orderId;
        private final String status;
        private final BigDecimal amount;
        private final String currency;
        private final LocalDateTime timestamp;
        
        public PaymentStatus(String orderId, String status, BigDecimal amount, String currency, LocalDateTime timestamp) {
            this.orderId = orderId;
            this.status = status;
            this.amount = amount;
            this.currency = currency;
            this.timestamp = timestamp;
        }
        
        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Test hash generation with known values for debugging
     */
    public void testHashGeneration() {
        String testMerchantId = "1231228";
        String testOrderId = "ORDER_TEST_123";
        String testAmount = "100.00";
        String testCurrency = "LKR";
        String testMerchantSecret = "2075125775410293987831000122153421658606";
        
        String hashString = testMerchantId + testOrderId + testAmount + testCurrency + testMerchantSecret;
        String hash = DigestUtils.md5Hex(hashString).toUpperCase();
        
        logger.info("Test hash generation:");
        logger.info("Merchant ID: {}", testMerchantId);
        logger.info("Order ID: {}", testOrderId);
        logger.info("Amount: {}", testAmount);
        logger.info("Currency: {}", testCurrency);
        logger.info("Hash string: {}", hashString);
        logger.info("Generated hash: {}", hash);
    }
}
