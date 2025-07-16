package com.islandhop.payment.controller;

import com.islandhop.payment.dto.PaymentRequest;
import com.islandhop.payment.dto.PaymentResponse;
import com.islandhop.payment.dto.PayHereNotification;
import com.islandhop.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController
 */
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    
    @Mock
    private PaymentService paymentService;
    
    @InjectMocks
    private PaymentController paymentController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testCreatePayHerePayment_Success() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        PaymentResponse response = PaymentResponse.success(
            request.getOrderId(), 
            "PAYMENT_123", 
            request.getAmount(), 
            request.getCurrency()
        );
        
        when(paymentService.createPayHerePayment(any(PaymentRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/payments/create-payhere-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.orderId").value(request.getOrderId()))
                .andExpect(jsonPath("$.amount").value(request.getAmount()))
                .andExpect(jsonPath("$.currency").value(request.getCurrency()));
    }
    
    @Test
    void testCreatePayHerePayment_ValidationError() throws Exception {
        // Arrange
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setOrderId(""); // Invalid - empty order ID
        
        // Act & Assert
        mockMvc.perform(post("/payments/create-payhere-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
    
    @Test
    void testCreatePayHerePayment_ServiceError() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        PaymentResponse errorResponse = PaymentResponse.error(
            request.getOrderId(), 
            "Service error"
        );
        
        when(paymentService.createPayHerePayment(any(PaymentRequest.class))).thenReturn(errorResponse);
        
        // Act & Assert
        mockMvc.perform(post("/payments/create-payhere-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.orderId").value(request.getOrderId()));
    }
    
    @Test
    void testHandlePayHereNotification_Success() throws Exception {
        // Arrange
        PayHereNotification notification = createValidSuccessNotification();
        PaymentResponse response = PaymentResponse.success(
            notification.getOrderId(), 
            notification.getPaymentId(), 
            notification.getAmount(), 
            notification.getCurrency()
        );
        
        when(paymentService.handlePayHereNotification(any(PayHereNotification.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/payments/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.orderId").value(notification.getOrderId()));
    }
    
    @Test
    void testHandlePayHereNotification_MissingOrderId() throws Exception {
        // Arrange
        PayHereNotification notification = new PayHereNotification();
        notification.setOrderId(null); // Missing order ID
        
        // Act & Assert
        mockMvc.perform(post("/payments/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
    
    @Test
    void testGetPaymentStatus_Success() throws Exception {
        // Arrange
        String orderId = "ORDER_TEST_123";
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setStatus("success");
        response.setAmount(new BigDecimal("100.00"));
        response.setCurrency("LKR");
        
        when(paymentService.getPaymentStatus(anyString())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/payments/status/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("LKR"));
    }
    
    @Test
    void testGetPaymentStatus_NotFound() throws Exception {
        // Arrange
        String orderId = "NON_EXISTENT_ORDER";
        PaymentResponse response = PaymentResponse.error(orderId, "Payment status not found");
        
        when(paymentService.getPaymentStatus(anyString())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/payments/status/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetPaymentStatus_EmptyOrderId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payments/status/{orderId}", ""))
                .andExpect(status().isNotFound()); // Spring returns 404 for empty path variable
    }
    
    @Test
    void testHealthCheck() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("PayHere Payment Service"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    // Helper methods
    
    private PaymentRequest createValidPaymentRequest() {
        PaymentRequest.CustomerDetails customerDetails = new PaymentRequest.CustomerDetails(
            "John", "Doe", "john.doe@example.com", "0771234567",
            "123 Main Street", "Colombo", "Sri Lanka"
        );
        
        return new PaymentRequest(
            new BigDecimal("100.00"),
            "LKR",
            "ORDER_TEST_123",
            "Test Trip Booking",
            customerDetails
        );
    }
    
    private PayHereNotification createValidSuccessNotification() {
        PayHereNotification notification = new PayHereNotification();
        notification.setOrderId("ORDER_TEST_123");
        notification.setStatusCode(2); // Success
        notification.setStatusMessage("Payment Successful");
        notification.setPaymentId("PAYMENT_123456");
        notification.setAmount(new BigDecimal("100.00"));
        notification.setCurrency("LKR");
        notification.setMerchantId("TEST_MERCHANT_ID");
        notification.setPayHereAmount(new BigDecimal("100.00"));
        notification.setPayHereCurrency("LKR");
        notification.setMd5Signature("test_signature");
        return notification;
    }
}
