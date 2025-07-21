package com.islandhop.payment.service;

import com.islandhop.payment.dto.PaymentRequest;
import com.islandhop.payment.dto.PaymentResponse;
import com.islandhop.payment.dto.PayHereNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @InjectMocks
    private PaymentService paymentService;
    
    @BeforeEach
    void setUp() {
        // Set up test configuration
        ReflectionTestUtils.setField(paymentService, "merchantId", "TEST_MERCHANT_ID");
        ReflectionTestUtils.setField(paymentService, "merchantSecret", "TEST_SECRET");
        ReflectionTestUtils.setField(paymentService, "notifyUrl", "http://localhost:8088/api/v1/payments/notify");
        ReflectionTestUtils.setField(paymentService, "sandbox", true);
    }
    
    @Test
    void testCreatePayHerePayment_Success() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        
        // Act
        PaymentResponse response = paymentService.createPayHerePayment(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(request.getCurrency(), response.getCurrency());
        assertNotNull(response.getPayHereData());
        assertTrue(response.getPayHereData().containsKey("merchant_id"));
        assertTrue(response.getPayHereData().containsKey("hash"));
    }
    
    @Test
    void testCreatePayHerePayment_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            paymentService.createPayHerePayment(null);
        });
    }
    
    @Test
    void testHandlePayHereNotification_Success() {
        // Arrange
        PayHereNotification notification = createValidSuccessNotification();
        
        // Act
        PaymentResponse response = paymentService.handlePayHereNotification(notification);
        
        // Assert
        assertNotNull(response);
        assertEquals(notification.getOrderId(), response.getOrderId());
        assertEquals(notification.getAmount(), response.getAmount());
        assertEquals(notification.getCurrency(), response.getCurrency());
    }
    
    @Test
    void testHandlePayHereNotification_Pending() {
        // Arrange
        PayHereNotification notification = createValidPendingNotification();
        
        // Act
        PaymentResponse response = paymentService.handlePayHereNotification(notification);
        
        // Assert
        assertNotNull(response);
        assertEquals("pending", response.getStatus());
        assertEquals(notification.getOrderId(), response.getOrderId());
    }
    
    @Test
    void testHandlePayHereNotification_Failed() {
        // Arrange
        PayHereNotification notification = createValidFailedNotification();
        
        // Act
        PaymentResponse response = paymentService.handlePayHereNotification(notification);
        
        // Assert
        assertNotNull(response);
        assertEquals("failure", response.getStatus());
        assertEquals(notification.getOrderId(), response.getOrderId());
    }
    
    @Test
    void testGetPaymentStatus_Found() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        paymentService.createPayHerePayment(request);
        
        // Act
        PaymentResponse response = paymentService.getPaymentStatus(request.getOrderId());
        
        // Assert
        assertNotNull(response);
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals("pending", response.getStatus());
    }
    
    @Test
    void testGetPaymentStatus_NotFound() {
        // Act
        PaymentResponse response = paymentService.getPaymentStatus("NON_EXISTENT_ORDER");
        
        // Assert
        assertNotNull(response);
        assertEquals("error", response.getStatus());
        assertEquals("NON_EXISTENT_ORDER", response.getOrderId());
    }
    
    @Test
    void testGetPaymentStatus_NullOrderId() {
        // Act
        PaymentResponse response = paymentService.getPaymentStatus(null);
        
        // Assert
        assertNotNull(response);
        assertEquals("error", response.getStatus());
    }
    
    @Test
    void testGetPaymentStatus_EmptyOrderId() {
        // Act
        PaymentResponse response = paymentService.getPaymentStatus("");
        
        // Assert
        assertNotNull(response);
        assertEquals("error", response.getStatus());
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
    
    private PayHereNotification createValidPendingNotification() {
        PayHereNotification notification = createValidSuccessNotification();
        notification.setStatusCode(1); // Pending
        notification.setStatusMessage("Payment Pending");
        return notification;
    }
    
    private PayHereNotification createValidFailedNotification() {
        PayHereNotification notification = createValidSuccessNotification();
        notification.setStatusCode(0); // Failed
        notification.setStatusMessage("Payment Failed");
        return notification;
    }
}
