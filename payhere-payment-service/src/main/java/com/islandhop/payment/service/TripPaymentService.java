package com.islandhop.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.islandhop.payment.model.PaidTrip;
import com.islandhop.payment.model.PaymentDetails;
import com.islandhop.payment.repository.PaidTripRepository;
import com.islandhop.payment.repository.PaymentDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling trip and payment data operations
 */
@Service
public class TripPaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(TripPaymentService.class);
    
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final PaidTripRepository paidTripRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public TripPaymentService(PaymentDetailsRepository paymentDetailsRepository,
                            PaidTripRepository paidTripRepository,
                            MongoTemplate mongoTemplate,
                            ObjectMapper objectMapper) {
        this.paymentDetailsRepository = paymentDetailsRepository;
        this.paidTripRepository = paidTripRepository;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Save payment details and move trip to paid trips collection
     * @param orderId Order ID
     * @param tripId Trip ID
     * @param userId User ID (can be null, will be extracted from trip data)
     * @param paymentAmount Payment amount
     * @param currency Currency
     * @param payHereTransactionId PayHere transaction ID
     * @param statusCode PayHere status code
     * @param statusMessage PayHere status message
     * @return true if successful, false otherwise
     */
    public boolean savePaymentAndMoveTripToPaid(String orderId, String tripId, String userId,
                                              BigDecimal paymentAmount, String currency,
                                              String payHereTransactionId, String statusCode,
                                              String statusMessage) {
        try {
            logger.info("Saving payment details and moving trip {} to paid collection", tripId);
            
            // 1. Retrieve initiated trip data
            Map<String, Object> initiatedTripData = getInitiatedTripData(tripId);
            if (initiatedTripData == null) {
                logger.error("Initiated trip not found for ID: {}", tripId);
                return false;
            }
            
            // 2. Extract user ID from trip data if not provided
            if (userId == null) {
                userId = (String) initiatedTripData.get("userId");
                if (userId == null) {
                    logger.error("User ID not found in trip data for trip: {}", tripId);
                    return false;
                }
            }
            
            // 3. Save payment details
            PaymentDetails paymentDetails = createPaymentDetails(orderId, tripId, userId, 
                    paymentAmount, currency, payHereTransactionId, statusCode, statusMessage);
            paymentDetailsRepository.save(paymentDetails);
            logger.info("Payment details saved for order: {}", orderId);
            
            // 4. Convert and save to paid trips
            PaidTrip paidTrip = convertToPaidTrip(initiatedTripData, paymentAmount);
            paidTripRepository.save(paidTrip);
            logger.info("Trip {} moved to paid trips collection", tripId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error saving payment and moving trip {} to paid collection", tripId, e);
            return false;
        }
    }
    
    /**
     * Create payment details entity
     */
    private PaymentDetails createPaymentDetails(String orderId, String tripId, String userId,
                                              BigDecimal paymentAmount, String currency,
                                              String payHereTransactionId, String statusCode,
                                              String statusMessage) {
        PaymentDetails paymentDetails = new PaymentDetails(orderId, tripId, userId, 
                paymentAmount, currency, "SUCCESS");
        
        paymentDetails.setPayHereTransactionId(payHereTransactionId);
        paymentDetails.setPayHereStatusCode(statusCode);
        paymentDetails.setPayHereStatusMessage(statusMessage);
        paymentDetails.setPaymentDate(Instant.now());
        
        return paymentDetails;
    }
    
    /**
     * Retrieve initiated trip data from MongoDB
     */
    private Map<String, Object> getInitiatedTripData(String tripId) {
        try {
            Query query = new Query(Criteria.where("_id").is(tripId));
            Map<String, Object> tripData = mongoTemplate.findOne(query, Map.class, "initiated_trips");
            
            if (tripData != null) {
                logger.info("Found initiated trip data for ID: {}", tripId);
                return tripData;
            } else {
                logger.warn("No initiated trip found for ID: {}", tripId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving initiated trip data for ID: {}", tripId, e);
            return null;
        }
    }
    
    /**
     * Convert initiated trip data to PaidTrip entity
     */
    private PaidTrip convertToPaidTrip(Map<String, Object> initiatedTripData, BigDecimal paymentAmount) {
        try {
            logger.info("Converting initiated trip data to paid trip. Data keys: {}", initiatedTripData.keySet());
            
            // Create new PaidTrip instance
            PaidTrip paidTrip = new PaidTrip();
            
            // Manually copy all fields from initiated trip data
            paidTrip.setId((String) initiatedTripData.get("_id"));
            paidTrip.setUserId((String) initiatedTripData.get("userId"));
            paidTrip.setTripName((String) initiatedTripData.get("tripName"));
            paidTrip.setStartDate((String) initiatedTripData.get("startDate"));
            paidTrip.setEndDate((String) initiatedTripData.get("endDate"));
            paidTrip.setArrivalTime((String) initiatedTripData.get("arrivalTime"));
            paidTrip.setBaseCity((String) initiatedTripData.get("baseCity"));
            paidTrip.setMultiCityAllowed((Boolean) initiatedTripData.get("multiCityAllowed"));
            paidTrip.setActivityPacing((String) initiatedTripData.get("activityPacing"));
            paidTrip.setBudgetLevel((String) initiatedTripData.get("budgetLevel"));
            
            // Handle lists
            if (initiatedTripData.get("preferredTerrains") != null) {
                paidTrip.setPreferredTerrains((List<String>) initiatedTripData.get("preferredTerrains"));
            }
            if (initiatedTripData.get("preferredActivities") != null) {
                paidTrip.setPreferredActivities((List<String>) initiatedTripData.get("preferredActivities"));
            }
            
            // Handle complex objects using ObjectMapper
            if (initiatedTripData.get("dailyPlans") != null) {
                List<PaidTrip.DailyPlan> dailyPlans = objectMapper.convertValue(
                    initiatedTripData.get("dailyPlans"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PaidTrip.DailyPlan.class)
                );
                paidTrip.setDailyPlans(dailyPlans);
            }
            
            if (initiatedTripData.get("mapData") != null) {
                List<PaidTrip.MapData> mapData = objectMapper.convertValue(
                    initiatedTripData.get("mapData"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PaidTrip.MapData.class)
                );
                paidTrip.setMapData(mapData);
            }
            
            // Handle timestamps
            if (initiatedTripData.get("createdAt") != null) {
                Object createdAtObj = initiatedTripData.get("createdAt");
                if (createdAtObj instanceof Map) {
                    Map<String, Object> dateMap = (Map<String, Object>) createdAtObj;
                    if (dateMap.containsKey("$date")) {
                        String dateStr = (String) dateMap.get("$date");
                        paidTrip.setCreatedAt(Instant.parse(dateStr));
                    }
                } else if (createdAtObj instanceof Instant) {
                    paidTrip.setCreatedAt((Instant) createdAtObj);
                }
            }
            
            // Handle numeric fields
            if (initiatedTripData.get("driverNeeded") != null) {
                paidTrip.setDriverNeeded((Integer) initiatedTripData.get("driverNeeded"));
            }
            if (initiatedTripData.get("guideNeeded") != null) {
                paidTrip.setGuideNeeded((Integer) initiatedTripData.get("guideNeeded"));
            }
            if (initiatedTripData.get("averageTripDistance") != null) {
                Object distanceObj = initiatedTripData.get("averageTripDistance");
                if (distanceObj instanceof Number) {
                    paidTrip.setAverageTripDistance(((Number) distanceObj).doubleValue());
                }
            }
            if (initiatedTripData.get("averageDriverCost") != null) {
                Object costObj = initiatedTripData.get("averageDriverCost");
                if (costObj instanceof Number) {
                    paidTrip.setAverageDriverCost(((Number) costObj).intValue());
                }
            }
            if (initiatedTripData.get("averageGuideCost") != null) {
                Object costObj = initiatedTripData.get("averageGuideCost");
                if (costObj instanceof Number) {
                    paidTrip.setAverageGuideCost(((Number) costObj).intValue());
                }
            }
            if (initiatedTripData.get("vehicleType") != null) {
                paidTrip.setVehicleType((String) initiatedTripData.get("vehicleType"));
            }
            
            // Set payment-specific fields
            paidTrip.setDriverStatus("");
            paidTrip.setDriverEmail("");
            paidTrip.setGuideStatus("");
            paidTrip.setGuideEmail("");
            paidTrip.setPayedAmount(paymentAmount);
            paidTrip.setLastUpdated(Instant.now());
            
            logger.info("Successfully converted initiated trip to paid trip for ID: {}", paidTrip.getId());
            logger.info("Copied trip data: tripName={}, userId={}, startDate={}, endDate={}", 
                       paidTrip.getTripName(), paidTrip.getUserId(), paidTrip.getStartDate(), paidTrip.getEndDate());
            
            return paidTrip;
            
        } catch (Exception e) {
            logger.error("Error converting initiated trip to paid trip", e);
            throw new RuntimeException("Failed to convert initiated trip to paid trip", e);
        }
    }
    
    /**
     * Get payment details by order ID
     * @param orderId Order ID
     * @return Optional PaymentDetails
     */
    public Optional<PaymentDetails> getPaymentDetailsByOrderId(String orderId) {
        return paymentDetailsRepository.findByOrderId(orderId);
    }
    
    /**
     * Save initial payment details when payment is created (before notification)
     * @param orderId Order ID
     * @param tripId Trip ID (can be null, will be extracted from order ID)
     * @param userId User ID (can be null, will be extracted from trip data)
     * @param paymentAmount Payment amount
     * @param currency Currency
     * @param paymentStatus Payment status
     * @return true if successful, false otherwise
     */
    public boolean saveInitialPaymentDetails(String orderId, String tripId, String userId,
                                           BigDecimal paymentAmount, String currency, String paymentStatus) {
        try {
            logger.info("Saving initial payment details for order: {} with tripId: {}", orderId, tripId);
            
            // Extract trip ID from order ID if not provided
            if (tripId == null) {
                tripId = extractTripIdFromOrderId(orderId);
                logger.info("Extracted trip ID from order ID: {}", tripId);
            }
            
            // If we have a trip ID, try to get user ID from trip data and migrate trip
            if (tripId != null && userId == null) {
                Map<String, Object> initiatedTripData = getInitiatedTripData(tripId);
                if (initiatedTripData != null) {
                    userId = (String) initiatedTripData.get("userId");
                    logger.info("Extracted user ID {} from trip data for trip: {}", userId, tripId);
                    
                    // Migrate trip to paid trips collection immediately
                    try {
                        PaidTrip paidTrip = convertToPaidTrip(initiatedTripData, paymentAmount);
                        paidTripRepository.save(paidTrip);
                        logger.info("Trip {} migrated to paid trips collection", tripId);
                    } catch (Exception e) {
                        logger.error("Error migrating trip {} to paid collection", tripId, e);
                    }
                } else {
                    logger.warn("No initiated trip data found for trip ID: {}", tripId);
                }
            }
            
            // Create and save payment details
            PaymentDetails paymentDetails = new PaymentDetails(orderId, tripId, userId, 
                    paymentAmount, currency, paymentStatus);
            paymentDetails.setPaymentDate(null); // Will be set when payment is completed
            
            paymentDetailsRepository.save(paymentDetails);
            logger.info("Initial payment details saved for order: {}", orderId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error saving initial payment details for order: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Update payment details when payment is completed (after notification)
     * @param orderId Order ID
     * @param payHereTransactionId PayHere transaction ID
     * @param statusCode PayHere status code
     * @param statusMessage PayHere status message
     * @param paymentStatus Final payment status
     * @return true if successful, false otherwise
     */
    public boolean updatePaymentDetailsOnCompletion(String orderId, String payHereTransactionId,
                                                   String statusCode, String statusMessage, String paymentStatus) {
        try {
            logger.info("Updating payment details for completed payment: {}", orderId);
            
            Optional<PaymentDetails> paymentDetailsOpt = paymentDetailsRepository.findByOrderId(orderId);
            if (paymentDetailsOpt.isEmpty()) {
                logger.warn("No existing payment details found for order: {}", orderId);
                return false;
            }
            
            PaymentDetails paymentDetails = paymentDetailsOpt.get();
            paymentDetails.setPayHereTransactionId(payHereTransactionId);
            paymentDetails.setPayHereStatusCode(statusCode);
            paymentDetails.setPayHereStatusMessage(statusMessage);
            paymentDetails.setPaymentStatus(paymentStatus);
            paymentDetails.setPaymentDate(Instant.now());
            paymentDetails.setLastUpdated(Instant.now());
            
            paymentDetailsRepository.save(paymentDetails);
            logger.info("Payment details updated for order: {}", orderId);
            
            // If payment is successful and we have trip data, move to paid trips
            if ("SUCCESS".equals(paymentStatus) && paymentDetails.getTripId() != null) {
                moveTripToPaidCollection(paymentDetails);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error updating payment details for order: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Move trip to paid collection after successful payment
     */
    private boolean moveTripToPaidCollection(PaymentDetails paymentDetails) {
        try {
            String tripId = paymentDetails.getTripId();
            logger.info("Moving trip {} to paid trips collection", tripId);
            
            // Retrieve initiated trip data
            Map<String, Object> initiatedTripData = getInitiatedTripData(tripId);
            if (initiatedTripData == null) {
                logger.error("Initiated trip not found for ID: {}", tripId);
                return false;
            }
            
            // Convert and save to paid trips
            PaidTrip paidTrip = convertToPaidTrip(initiatedTripData, paymentDetails.getAmount());
            paidTripRepository.save(paidTrip);
            logger.info("Trip {} moved to paid trips collection", tripId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error moving trip to paid collection", e);
            return false;
        }
    }
    
    /**
     * Get paid trip by trip ID
     * @param tripId Trip ID
     * @return Optional PaidTrip
     */
    public Optional<PaidTrip> getPaidTripById(String tripId) {
        return paidTripRepository.findById(tripId);
    }
    
    /**
     * Extract trip ID from order ID
     * Order ID format is expected to be like: "trip_042cea55-afae-4c2e-9d19-e3c40490579b_payment"
     * @param orderId Order ID
     * @return Trip ID or null if format is invalid
     */
    public String extractTripIdFromOrderId(String orderId) {
        try {
            if (orderId != null && orderId.startsWith("trip_") && orderId.endsWith("_payment")) {
                String tripId = orderId.substring(5, orderId.length() - 8); // Remove "trip_" and "_payment"
                logger.debug("Extracted trip ID {} from order ID {}", tripId, orderId);
                return tripId;
            }
            logger.warn("Invalid order ID format: {}", orderId);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting trip ID from order ID: {}", orderId, e);
            return null;
        }
    }
    
    /**
     * Process payment confirmation from frontend and move trip to paid collection
     * @param tripId Trip ID
     * @param userId User ID
     * @param paymentOrderId Payment order ID
     * @param paidAmount Amount paid
     * @param paymentType Payment type (e.g., "advance_payment")
     * @param paymentStatus Payment status (e.g., "completed")
     * @return true if successful, false otherwise
     */
    public boolean processPaymentConfirmationAndMoveTrip(String tripId, String userId, String paymentOrderId,
                                                        BigDecimal paidAmount, String paymentType, String paymentStatus) {
        try {
            logger.info("Processing payment confirmation for trip: {} with order: {}", tripId, paymentOrderId);
            
            // 1. Save or update payment details
            PaymentDetails paymentDetails = new PaymentDetails(paymentOrderId, tripId, userId, 
                    paidAmount, "LKR", "SUCCESS");
            paymentDetails.setPaymentDate(Instant.now());
            paymentDetails.setPayHereStatusMessage("Payment confirmed via frontend");
            
            // Check if payment details already exist
            Optional<PaymentDetails> existingPayment = paymentDetailsRepository.findByOrderId(paymentOrderId);
            if (existingPayment.isPresent()) {
                PaymentDetails existing = existingPayment.get();
                existing.setPaymentStatus("SUCCESS");
                existing.setPaymentDate(Instant.now());
                existing.setLastUpdated(Instant.now());
                paymentDetailsRepository.save(existing);
                logger.info("Updated existing payment details for order: {}", paymentOrderId);
            } else {
                paymentDetailsRepository.save(paymentDetails);
                logger.info("Created new payment details for order: {}", paymentOrderId);
            }
            
            // 2. Retrieve initiated trip data from trip-initiation-microservice
            Map<String, Object> initiatedTripData = getInitiatedTripDataFromService(tripId);
            if (initiatedTripData == null) {
                logger.error("Failed to retrieve initiated trip data for ID: {}", tripId);
                return false;
            }
            
            // 3. Convert and save to paid trips collection
            PaidTrip paidTrip = convertToPaidTrip(initiatedTripData, paidAmount);
            
            // Set payment-specific fields
            paidTrip.setDriverStatus("");
            paidTrip.setDriverEmail("");
            paidTrip.setGuideStatus("");
            paidTrip.setGuideEmail("");
            paidTrip.setPayedAmount(paidAmount);
            
            paidTripRepository.save(paidTrip);
            logger.info("Trip {} moved to paid trips collection with payment amount: {}", tripId, paidAmount);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing payment confirmation for trip: {}", tripId, e);
            return false;
        }
    }
    
    /**
     * Retrieve initiated trip data from trip-initiation-microservice via HTTP call
     */
    private Map<String, Object> getInitiatedTripDataFromService(String tripId) {
        try {
            // First try to get from local MongoDB (initiated_trips collection)
            Map<String, Object> tripData = getInitiatedTripData(tripId);
            if (tripData != null) {
                logger.info("Found trip data in local MongoDB for ID: {}", tripId);
                return tripData;
            }
            
            // If not found locally, try to fetch from trip-initiation-microservice
            logger.info("Trip data not found locally, fetching from trip-initiation-microservice for ID: {}", tripId);
            
            // Note: You may need to implement HTTP client call to trip-initiation-microservice
            // For now, we'll assume the data should be in local MongoDB
            logger.warn("Trip data not found in local MongoDB for ID: {}", tripId);
            return null;
            
        } catch (Exception e) {
            logger.error("Error retrieving trip data for ID: {}", tripId, e);
            return null;
        }
    }
}
