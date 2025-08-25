package com.islandhop.payment.repository;

import com.islandhop.payment.model.PaymentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentDetails entity
 */
@Repository
public interface PaymentDetailsRepository extends MongoRepository<PaymentDetails, String> {
    
    /**
     * Find payment details by order ID
     * @param orderId Order ID
     * @return Optional PaymentDetails
     */
    Optional<PaymentDetails> findByOrderId(String orderId);
    
    /**
     * Find payment details by trip ID
     * @param tripId Trip ID
     * @return List of PaymentDetails
     */
    List<PaymentDetails> findByTripId(String tripId);
    
    /**
     * Find payment details by user ID
     * @param userId User ID
     * @return List of PaymentDetails
     */
    List<PaymentDetails> findByUserId(String userId);
    
    /**
     * Find payment details by payment status
     * @param paymentStatus Payment status
     * @return List of PaymentDetails
     */
    List<PaymentDetails> findByPaymentStatus(String paymentStatus);
    
    /**
     * Find payment details by user ID and payment status
     * @param userId User ID
     * @param paymentStatus Payment status
     * @return List of PaymentDetails
     */
    List<PaymentDetails> findByUserIdAndPaymentStatus(String userId, String paymentStatus);
}
