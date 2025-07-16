package com.islandhop.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * PayHere notification DTO for handling payment notifications
 */
public class PayHereNotification {
    
    @JsonProperty("order_id")
    private String orderId;
    
    @JsonProperty("status_code")
    private Integer statusCode;
    
    @JsonProperty("status_message")
    private String statusMessage;
    
    @JsonProperty("payment_id")
    private String paymentId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("md5sig")
    private String md5Signature;
    
    // Additional PayHere fields
    @JsonProperty("merchant_id")
    private String merchantId;
    
    @JsonProperty("payhere_amount")
    private BigDecimal payHereAmount;
    
    @JsonProperty("payhere_currency")
    private String payHereCurrency;
    
    @JsonProperty("customer_token")
    private String customerToken;
    
    @JsonProperty("recurring")
    private String recurring;
    
    @JsonProperty("card_holder_name")
    private String cardHolderName;
    
    @JsonProperty("card_no")
    private String cardNo;
    
    @JsonProperty("card_expiry")
    private String cardExpiry;
    
    // Constructors
    public PayHereNotification() {}
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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
    
    public String getMd5Signature() {
        return md5Signature;
    }
    
    public void setMd5Signature(String md5Signature) {
        this.md5Signature = md5Signature;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public BigDecimal getPayHereAmount() {
        return payHereAmount;
    }
    
    public void setPayHereAmount(BigDecimal payHereAmount) {
        this.payHereAmount = payHereAmount;
    }
    
    public String getPayHereCurrency() {
        return payHereCurrency;
    }
    
    public void setPayHereCurrency(String payHereCurrency) {
        this.payHereCurrency = payHereCurrency;
    }
    
    public String getCustomerToken() {
        return customerToken;
    }
    
    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }
    
    public String getRecurring() {
        return recurring;
    }
    
    public void setRecurring(String recurring) {
        this.recurring = recurring;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getCardNo() {
        return cardNo;
    }
    
    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    
    public String getCardExpiry() {
        return cardExpiry;
    }
    
    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }
    
    /**
     * Check if payment is successful based on status code
     * @return true if payment is successful (status code 2)
     */
    public boolean isPaymentSuccessful() {
        return statusCode != null && statusCode == 2;
    }
    
    /**
     * Check if payment is pending based on status code
     * @return true if payment is pending (status code 1)
     */
    public boolean isPaymentPending() {
        return statusCode != null && statusCode == 1;
    }
    
    /**
     * Check if payment is failed based on status code
     * @return true if payment is failed (status code 0 or -1, -2, -3)
     */
    public boolean isPaymentFailed() {
        return statusCode != null && (statusCode == 0 || statusCode < 0);
    }
    
    @Override
    public String toString() {
        return "PayHereNotification{" +
                "orderId='" + orderId + '\'' +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", md5Signature='" + md5Signature + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", payHereAmount=" + payHereAmount +
                ", payHereCurrency='" + payHereCurrency + '\'' +
                ", customerToken='" + customerToken + '\'' +
                ", recurring='" + recurring + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", cardExpiry='" + cardExpiry + '\'' +
                '}';
    }
}
