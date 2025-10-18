package com.islandhop.payment.service;

import com.islandhop.payment.model.PaymentDetails;
import com.islandhop.payment.model.PaidTrip;
import com.islandhop.payment.repository.PaymentDetailsRepository;
import com.islandhop.payment.repository.PaidTripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TripPaymentService
 */
@ExtendWith(MockitoExtension.class)
class TripPaymentServiceTest {

    @Mock
    private PaymentDetailsRepository paymentDetailsRepository;

    @Mock
    private PaidTripRepository paidTripRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TripPaymentService tripPaymentService;

    private Map<String, Object> mockTripData;
    private String testTripId;
    private String testOrderId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testTripId = "042cea55-afae-4c2e-9d19-e3c40490579b";
        testOrderId = "trip_" + testTripId + "_payment";
        testUserId = "J0INIUkpCDNpUHCUkY0xmyPwoEe2";

        // Mock trip data
        mockTripData = new HashMap<>();
        mockTripData.put("_id", testTripId);
        mockTripData.put("userId", testUserId);
        mockTripData.put("tripName", "testtrip1");
        mockTripData.put("startDate", "2025-08-26");
        mockTripData.put("endDate", "2025-08-28");
        mockTripData.put("baseCity", "Colombo");
        mockTripData.put("driverNeeded", 1);
        mockTripData.put("guideNeeded", 1);
        mockTripData.put("averageTripDistance", 400.86);
        mockTripData.put("averageDriverCost", 60279);
        mockTripData.put("averageGuideCost", 0);
        mockTripData.put("vehicleType", "Luxury Van");
    }

    @Test
    void testExtractTripIdFromOrderId_ValidFormat() {
        String result = tripPaymentService.extractTripIdFromOrderId(testOrderId);
        assertEquals(testTripId, result);
    }

    @Test
    void testExtractTripIdFromOrderId_InvalidFormat() {
        String result = tripPaymentService.extractTripIdFromOrderId("invalid_order_id");
        assertNull(result);
    }

    @Test
    void testExtractTripIdFromOrderId_NullInput() {
        String result = tripPaymentService.extractTripIdFromOrderId(null);
        assertNull(result);
    }

    @Test
    void testSavePaymentAndMoveTripToPaid_Success() {
        // Arrange
        when(mongoTemplate.findOne(any(Query.class), eq(Map.class), eq("initiated_trips")))
                .thenReturn(mockTripData);
        
        PaidTrip mockPaidTrip = new PaidTrip();
        mockPaidTrip.setId(testTripId);
        mockPaidTrip.setUserId(testUserId);
        mockPaidTrip.setPayedAmount(BigDecimal.valueOf(75.00));
        
        when(objectMapper.convertValue(mockTripData, PaidTrip.class))
                .thenReturn(mockPaidTrip);
        
        when(paymentDetailsRepository.save(any(PaymentDetails.class)))
                .thenReturn(new PaymentDetails());
        
        when(paidTripRepository.save(any(PaidTrip.class)))
                .thenReturn(mockPaidTrip);

        // Act
        boolean result = tripPaymentService.savePaymentAndMoveTripToPaid(
                testOrderId, testTripId, null, BigDecimal.valueOf(75.00), "LKR",
                "payhere-txn-123", "2", "Success"
        );

        // Assert
        assertTrue(result);
        verify(paymentDetailsRepository, times(1)).save(any(PaymentDetails.class));
        verify(paidTripRepository, times(1)).save(any(PaidTrip.class));
        verify(mongoTemplate, times(1)).findOne(any(Query.class), eq(Map.class), eq("initiated_trips"));
    }

    @Test
    void testSavePaymentAndMoveTripToPaid_TripNotFound() {
        // Arrange
        when(mongoTemplate.findOne(any(Query.class), eq(Map.class), eq("initiated_trips")))
                .thenReturn(null);

        // Act
        boolean result = tripPaymentService.savePaymentAndMoveTripToPaid(
                testOrderId, testTripId, testUserId, BigDecimal.valueOf(75.00), "LKR",
                "payhere-txn-123", "2", "Success"
        );

        // Assert
        assertFalse(result);
        verify(paymentDetailsRepository, never()).save(any(PaymentDetails.class));
        verify(paidTripRepository, never()).save(any(PaidTrip.class));
    }

    @Test
    void testGetPaymentDetailsByOrderId() {
        // Arrange
        PaymentDetails mockPaymentDetails = new PaymentDetails();
        mockPaymentDetails.setOrderId(testOrderId);
        when(paymentDetailsRepository.findByOrderId(testOrderId))
                .thenReturn(Optional.of(mockPaymentDetails));

        // Act
        Optional<PaymentDetails> result = tripPaymentService.getPaymentDetailsByOrderId(testOrderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOrderId, result.get().getOrderId());
        verify(paymentDetailsRepository, times(1)).findByOrderId(testOrderId);
    }

    @Test
    void testGetPaidTripById() {
        // Arrange
        PaidTrip mockPaidTrip = new PaidTrip();
        mockPaidTrip.setId(testTripId);
        when(paidTripRepository.findById(testTripId))
                .thenReturn(Optional.of(mockPaidTrip));

        // Act
        Optional<PaidTrip> result = tripPaymentService.getPaidTripById(testTripId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTripId, result.get().getId());
        verify(paidTripRepository, times(1)).findById(testTripId);
    }
}
