package com.islandhop.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment response DTO for PayHere payment operations
 */
public class PaymentResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("paymentId")
    private String paymentId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("payHereData")
    private Map<String, Object> payHereData;
    
    // Constructors
    public PaymentResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public PaymentResponse(String status, String message, String orderId) {
        this();
        this.status = status;
        this.message = message;
        this.orderId = orderId;
    }
    
    public PaymentResponse(String status, String message, String orderId, String paymentId, BigDecimal amount, String currency) {
        this(status, message, orderId);
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
    }
    
    // Static factory methods for common responses
    public static PaymentResponse success(String orderId, String paymentId, BigDecimal amount, String currency) {
        return new PaymentResponse("success", "Payment processed successfully", orderId, paymentId, amount, currency);
    }
    
    public static PaymentResponse failure(String orderId, String message) {
        return new PaymentResponse("failure", message, orderId);
    }
    
    public static PaymentResponse error(String orderId, String message) {
        return new PaymentResponse("error", message, orderId);
    }
    
    public static PaymentResponse pending(String orderId, String message) {
        return new PaymentResponse("pending", message, orderId);
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getPayHereData() {
        return payHereData;
    }
    
    public void setPayHereData(Map<String, Object> payHereData) {
        this.payHereData = payHereData;
    }
    
    @Override
    public String toString() {
        return "PaymentResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", orderId='" + orderId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", timestamp=" + timestamp +
                ", payHereData=" + payHereData +
                '}';
    }
}
