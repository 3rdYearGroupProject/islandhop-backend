package com.islandhop.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment details entity for MongoDB storage
 */
@Document(collection = "payment_details")
public class PaymentDetails {
    
    @Id
    private String paymentId;
    private String orderId;
    private String tripId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String payHereTransactionId;
    private String payHereStatusCode;
    private String payHereStatusMessage;
    private Instant paymentDate;
    private Instant createdAt;
    private Instant lastUpdated;
    
    // Constructors
    public PaymentDetails() {}
    
    public PaymentDetails(String orderId, String tripId, String userId, BigDecimal amount, 
                         String currency, String paymentStatus) {
        this.orderId = orderId;
        this.tripId = tripId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = "PayHere";
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
    }
    
    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getTripId() {
        return tripId;
    }
    
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
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
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPayHereTransactionId() {
        return payHereTransactionId;
    }
    
    public void setPayHereTransactionId(String payHereTransactionId) {
        this.payHereTransactionId = payHereTransactionId;
    }
    
    public String getPayHereStatusCode() {
        return payHereStatusCode;
    }
    
    public void setPayHereStatusCode(String payHereStatusCode) {
        this.payHereStatusCode = payHereStatusCode;
    }
    
    public String getPayHereStatusMessage() {
        return payHereStatusMessage;
    }
    
    public void setPayHereStatusMessage(String payHereStatusMessage) {
        this.payHereStatusMessage = payHereStatusMessage;
    }
    
    public Instant getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    @Override
    public String toString() {
        return "PaymentDetails{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", paymentDate=" + paymentDate +
                '}';
    }
}
