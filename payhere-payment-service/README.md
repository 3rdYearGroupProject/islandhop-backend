# PayHere Payment Service

A comprehensive Spring Boot microservice for handling PayHere payment integration in the IslandHop travel system.

## Features

- **PayHere Integration**: Complete integration with PayHere payment gateway
- **Secure Payment Processing**: MD5 hash generation and signature verification
- **Payment Status Tracking**: Real-time payment status monitoring
- **Notification Handling**: Automated processing of PayHere notifications
- **RESTful API**: Clean REST endpoints for frontend integration
- **Comprehensive Logging**: Detailed logging for debugging and auditing
- **Input Validation**: Robust validation using Spring Validation
- **Unit & Integration Tests**: Complete test coverage

## Technology Stack

- **Java 17**: Modern Java features and performance
- **Spring Boot 3.3.2**: Latest Spring Boot framework
- **Spring Web**: RESTful web services
- **Spring Validation**: Input validation
- **Maven**: Dependency management and build tool
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for tests

## API Endpoints

### 1. Create PayHere Payment

**POST** `/api/v1/payments/create-payhere-payment`

Creates a new PayHere payment request with all necessary data.

**Request Body:**

```json
{
  "amount": 75.0,
  "currency": "LKR",
  "orderId": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "itemName": "Trip Booking",
  "customerDetails": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "tourist013@gmail.com",
    "phone": "0771234567",
    "address": "123 Main Street",
    "city": "Colombo",
    "country": "Sri Lanka"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "message": "PayHere payment data generated successfully",
  "orderId": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "amount": 75.0,
  "currency": "LKR",
  "timestamp": "2025-07-15T10:30:00",
  "payHereData": {
    "merchant_id": "1234567",
    "order_id": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
    "amount": "75.00",
    "currency": "LKR",
    "items": "Trip Booking",
    "first_name": "John",
    "last_name": "Doe",
    "email": "tourist013@gmail.com",
    "phone": "0771234567",
    "address": "123 Main Street",
    "city": "Colombo",
    "country": "Sri Lanka",
    "return_url": "http://localhost:3000/payment/success",
    "cancel_url": "http://localhost:3000/payment/cancel",
    "notify_url": "http://localhost:8088/api/v1/payments/notify",
    "hash": "generated_hash_value"
  }
}
```

### 2. Handle PayHere Notification

**POST** `/api/v1/payments/notify`

Handles PayHere payment notifications (success, failure, pending).

**Request Body:**

```json
{
  "order_id": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "status_code": 2,
  "status_message": "Payment Successful",
  "payment_id": "PAYMENT_123456",
  "amount": 75.0,
  "currency": "LKR",
  "merchant_id": "1234567",
  "payhere_amount": 75.0,
  "payhere_currency": "LKR",
  "md5sig": "notification_signature"
}
```

**Response:**

```json
{
  "status": "success",
  "message": "Payment processed successfully",
  "orderId": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "paymentId": "PAYMENT_123456",
  "amount": 75.0,
  "currency": "LKR",
  "timestamp": "2025-07-15T10:35:00"
}
```

### 3. Get Payment Status

**GET** `/api/v1/payments/status/{orderId}`

Retrieves the current payment status for a specific order.

**Response:**

```json
{
  "status": "success",
  "message": "Payment status retrieved successfully",
  "orderId": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "amount": 75.0,
  "currency": "LKR",
  "timestamp": "2025-07-15T10:35:00"
}
```

### 4. Health Check

**GET** `/api/v1/payments/health`

Returns service health status.

**Response:**

```json
{
  "status": "UP",
  "service": "PayHere Payment Service",
  "timestamp": 1721041200000
}
```

## Environment Variables

Configure the following environment variables:

```bash
# PayHere Configuration
PAYHERE_MERCHANT_ID=your_merchant_id
PAYHERE_SECRET=your_secret_key
PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
PAYHERE_SANDBOX=true

# Server Configuration
SERVER_PORT=8088
```

## Payment Status Codes

PayHere uses the following status codes:

- **2**: Payment Successful
- **1**: Payment Pending
- **0**: Payment Failed
- **-1**: Payment Cancelled
- **-2**: Payment Error
- **-3**: Payment Timeout

## Security Features

1. **MD5 Hash Verification**: All payment requests include MD5 hash for security
2. **Signature Verification**: PayHere notifications are verified using MD5 signatures
3. **Input Validation**: All inputs are validated using Spring Validation
4. **IP Logging**: Client IP addresses are logged for security auditing
5. **Environment Variables**: Sensitive data stored in environment variables

## Installation & Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PayHere merchant account

### Build and Run

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd payhere-payment-service
   ```

2. **Set environment variables**

   ```bash
   export PAYHERE_MERCHANT_ID=your_merchant_id
   export PAYHERE_SECRET=your_secret_key
   export PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
   export PAYHERE_SANDBOX=true
   ```

3. **Build the application**

   ```bash
   mvn clean compile
   ```

4. **Run tests**

   ```bash
   mvn test
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The service will start on port 8088.

### Docker Deployment

1. **Build Docker image**

   ```bash
   docker build -t payhere-payment-service .
   ```

2. **Run Docker container**
   ```bash
   docker run -p 8088:8088 \
     -e PAYHERE_MERCHANT_ID=your_merchant_id \
     -e PAYHERE_SECRET=your_secret_key \
     -e PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify \
     -e PAYHERE_SANDBOX=true \
     payhere-payment-service
   ```

## Testing

### Unit Tests

Run unit tests for service layer:

```bash
mvn test -Dtest=PaymentServiceTest
```

### Integration Tests

Run integration tests for controller layer:

```bash
mvn test -Dtest=PaymentControllerTest
```

### All Tests

Run all tests:

```bash
mvn test
```

## Frontend Integration

### JavaScript Example

```javascript
// Create PayHere payment
const createPayment = async (paymentData) => {
  try {
    const response = await fetch(
      "http://localhost:8088/api/v1/payments/create-payhere-payment",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(paymentData),
      }
    );

    const result = await response.json();

    if (result.status === "success") {
      // Use result.payHereData to initialize PayHere payment
      initiatePayHerePayment(result.payHereData);
    } else {
      console.error("Payment creation failed:", result.message);
    }
  } catch (error) {
    console.error("Error creating payment:", error);
  }
};

// Check payment status
const checkPaymentStatus = async (orderId) => {
  try {
    const response = await fetch(
      `http://localhost:8088/api/v1/payments/status/${orderId}`
    );
    const result = await response.json();

    return result;
  } catch (error) {
    console.error("Error checking payment status:", error);
    return null;
  }
};
```

## Logging

The service provides comprehensive logging:

- **INFO**: General application flow
- **DEBUG**: Detailed debugging information
- **WARN**: Warning conditions
- **ERROR**: Error conditions

Log files are stored in `logs/payhere-payment-service.log`

## Configuration

### Application Properties

See `src/main/resources/application.yml` for all configuration options.

### Profile-specific Configuration

- `application-dev.yml`: Development configuration
- `application-prod.yml`: Production configuration

## Monitoring

### Health Check

Use the health endpoint to monitor service status:

```bash
curl http://localhost:8088/api/v1/payments/health
```

### Management Endpoints

Available management endpoints:

- `/actuator/health`: Health information
- `/actuator/info`: Application information
- `/actuator/metrics`: Application metrics

## Error Handling

The service provides comprehensive error handling:

- **400 Bad Request**: Invalid request data
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server errors

All errors include detailed error messages and timestamps.

## Support

For support and issues, please contact the development team or create an issue in the repository.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
