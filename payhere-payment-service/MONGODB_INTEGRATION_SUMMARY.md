# PayHere Payment Service - MongoDB Integration Implementation

## Overview

This document describes the implementation of MongoDB integration for the PayHere Payment Service to save payment details and trip data after successful payments.

## Features Implemented

### 1. MongoDB Collections Created

#### a. `payment_details` Collection

- **Purpose**: Stores detailed payment transaction information
- **Database**: `islandhop_trips`
- **Fields**:
  - `paymentId`: Unique payment identifier
  - `orderId`: Order identifier from payment request
  - `tripId`: Related trip identifier
  - `userId`: User who made the payment
  - `amount`: Payment amount
  - `currency`: Payment currency
  - `paymentMethod`: Always "PayHere"
  - `paymentStatus`: Payment status (SUCCESS, PENDING, FAILED)
  - `payHereTransactionId`: PayHere transaction ID
  - `payHereStatusCode`: PayHere status code
  - `payHereStatusMessage`: PayHere status message
  - `paymentDate`: Date when payment was completed
  - `createdAt`: Record creation timestamp
  - `lastUpdated`: Record last update timestamp

#### b. `payed_trips_advance` Collection

- **Purpose**: Stores trip data after successful payment with additional fields
- **Database**: `islandhop_trips`
- **Fields**: All fields from `initiated_trips` collection plus:
  - `driverStatus`: Driver assignment status (initially null)
  - `driverEmail`: Driver email (initially null)
  - `guideStatus`: Guide assignment status (initially null)
  - `guideEmail`: Guide email (initially null)
  - `payedAmount`: Amount paid for the trip

### 2. New Components Added

#### Entities/Models

- `PaymentDetails.java`: Entity for payment details collection
- `PaidTrip.java`: Entity for paid trips collection with nested classes for complex structures

#### Repositories

- `PaymentDetailsRepository.java`: Repository for payment details operations
- `PaidTripRepository.java`: Repository for paid trips operations
- `InitiatedTripRepository.java`: Repository for accessing initiated trips

#### Services

- `TripPaymentService.java`: Service for handling trip and payment data operations

#### Updated Components

- Updated `PaymentService.java` to integrate with MongoDB operations
- Updated `PaymentController.java` with new endpoints

### 3. New API Endpoints

#### a. Get Paid Trip Details

- **Endpoint**: `GET /api/v1/payments/paid-trip/{tripId}`
- **Purpose**: Retrieve paid trip details by trip ID
- **Response**: Complete trip data with payment information

#### b. Get Payment Details

- **Endpoint**: `GET /api/v1/payments/payment-details/{orderId}`
- **Purpose**: Retrieve payment transaction details by order ID
- **Response**: Complete payment transaction information

### 4. Enhanced Payment Flow

#### When a successful payment notification is received:

1. **Payment Verification**: Verify PayHere notification signature
2. **Trip Data Retrieval**: Fetch trip data from `initiated_trips` collection
3. **Payment Details Storage**: Save payment transaction details to `payment_details` collection
4. **Trip Migration**: Copy trip data to `payed_trips_advance` collection with additional fields
5. **Status Update**: Update payment status in memory cache

### 5. Configuration Updates

#### Dependencies Added

- `spring-boot-starter-data-mongodb`: MongoDB support

#### Application Configuration

- MongoDB connection string: `mongodb+srv://2022cs056:dH4aTFn3IOerWlVZ@cluster0.9ccambx.mongodb.net/islandhop_trips`
- Database: `islandhop_trips`

### 6. Order ID Format

The system expects order IDs in the format: `trip_{tripId}_payment`

- Example: `trip_042cea55-afae-4c2e-9d19-e3c40490579b_payment`
- This allows extraction of trip ID for database operations

### 7. Error Handling

- Comprehensive error handling for MongoDB operations
- Graceful handling of missing trip data
- Detailed logging for debugging and monitoring
- Non-blocking error handling (payment success is still reported to PayHere even if database operations fail)

### 8. Testing

- Unit tests for `TripPaymentService`
- Test configuration for isolated testing
- Mock-based testing for repository operations

## Technical Architecture

```
Payment Notification → PaymentService → TripPaymentService → MongoDB
                              ↓
                        PaymentController ← Client Requests
```

### Data Flow

1. **Payment Creation**: Client creates payment via `/create-payhere-payment`
2. **Payment Processing**: PayHere processes payment and sends notification
3. **Notification Handling**: Service receives notification via `/notify` or `/notify-form`
4. **Database Operations**:
   - Save payment details to `payment_details`
   - Migrate trip from `initiated_trips` to `payed_trips_advance`
5. **Client Queries**: Frontend can query paid trips and payment details

## Usage Examples

### Frontend Integration

```javascript
// Get paid trip details
const tripDetails = await fetch(`/api/v1/payments/paid-trip/${tripId}`);
const trip = await tripDetails.json();

// Get payment details
const paymentDetails = await fetch(
  `/api/v1/payments/payment-details/${orderId}`
);
const payment = await paymentDetails.json();
```

### Expected Data Structures

#### Paid Trip Response

```json
{
  "status": "success",
  "data": {
    "id": "042cea55-afae-4c2e-9d19-e3c40490579b",
    "userId": "J0INIUkpCDNpUHCUkY0xmyPwoEe2",
    "tripName": "testtrip1",
    "driverStatus": null,
    "driverEmail": null,
    "guideStatus": null,
    "guideEmail": null,
    "payedAmount": 75.0
    // ... other trip fields
  }
}
```

#### Payment Details Response

```json
{
  "status": "success",
  "data": {
    "paymentId": "generated-id",
    "orderId": "trip_042cea55-afae-4c2e-9d19-e3c40490579b_payment",
    "tripId": "042cea55-afae-4c2e-9d19-e3c40490579b",
    "userId": "J0INIUkpCDNpUHCUkY0xmyPwoEe2",
    "amount": 75.0,
    "currency": "LKR",
    "paymentStatus": "SUCCESS"
    // ... other payment fields
  }
}
```

## Files Modified/Created

### New Files Created

- `src/main/java/com/islandhop/payment/model/PaymentDetails.java`
- `src/main/java/com/islandhop/payment/model/PaidTrip.java`
- `src/main/java/com/islandhop/payment/repository/PaymentDetailsRepository.java`
- `src/main/java/com/islandhop/payment/repository/PaidTripRepository.java`
- `src/main/java/com/islandhop/payment/repository/InitiatedTripRepository.java`
- `src/main/java/com/islandhop/payment/service/TripPaymentService.java`
- `src/test/java/com/islandhop/payment/service/TripPaymentServiceTest.java`
- `src/test/resources/application-test.yml`

### Modified Files

- `pom.xml`: Added MongoDB dependency
- `src/main/resources/application.yml`: Added MongoDB configuration
- `src/main/java/com/islandhop/payment/service/PaymentService.java`: Integrated MongoDB operations
- `src/main/java/com/islandhop/payment/controller/PaymentController.java`: Added new endpoints
- `frontend_integration.md`: Updated documentation with new endpoints

## Deployment Notes

1. **Database Setup**: Ensure MongoDB is accessible with provided connection string
2. **Collections**: Collections will be auto-created on first use
3. **Indexes**: Consider adding indexes on frequently queried fields:
   - `payment_details.orderId`
   - `payment_details.tripId`
   - `payment_details.userId`
   - `payed_trips_advance.userId`

## Future Enhancements

1. **Driver/Guide Assignment**: Implement APIs to update driver and guide status
2. **Payment History**: Add endpoint to get user's payment history
3. **Trip Analytics**: Add endpoints for trip and payment analytics
4. **Refund Processing**: Add support for payment refunds
5. **Notification System**: Add email/SMS notifications for successful payments

## Security Considerations

1. **API Security**: Endpoints should be secured with proper authentication
2. **Data Validation**: All input data is validated before database operations
3. **Error Handling**: Sensitive information is not exposed in error messages
4. **Audit Trail**: All payment operations are logged for audit purposes

This implementation provides a robust foundation for managing payments and trip data in the IslandHop travel system.
