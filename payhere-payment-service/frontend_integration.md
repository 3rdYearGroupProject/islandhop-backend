# Frontend Integration Guide - PayHere Payment Service

This guide provides detailed instructions for integrating the PayHere Payment Service with your frontend application.

## Base URL

```
http://localhost:8088/api/v1
```

## Endpoints

### 1. Create PayHere Payment

**POST** `/payments/create-payhere-payment`

#### Request Format

```javascript
const paymentData = {
  amount: 75.0,
  currency: "LKR",
  orderId: "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  itemName: "Trip Booking",
  customerDetails: {
    firstName: "John",
    lastName: "Doe",
    email: "tourist013@gmail.com",
    phone: "0771234567",
    address: "123 Main Street",
    city: "Colombo",
    country: "Sri Lanka",
  },
};
```

#### JavaScript Implementation

```javascript
const createPayHerePayment = async (paymentData) => {
  try {
    const response = await fetch(
      "http://localhost:8088/api/v1/payments/create-payhere-payment",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(paymentData),
      }
    );

    const result = await response.json();

    if (response.ok && result.status === "success") {
      // Success - result.payHereData contains PayHere form data
      console.log("PayHere data:", result.payHereData);
      return result;
    } else {
      // Error handling
      console.error("Payment creation failed:", result.message);
      throw new Error(result.message || "Payment creation failed");
    }
  } catch (error) {
    console.error("Error creating payment:", error);
    throw error;
  }
};

// Usage
const paymentData = {
  amount: 75.0,
  currency: "LKR",
  orderId: "ORDER_" + Date.now(),
  itemName: "Trip Booking",
  customerDetails: {
    firstName: "John",
    lastName: "Doe",
    email: "john.doe@example.com",
    phone: "0771234567",
    address: "123 Main Street",
    city: "Colombo",
    country: "Sri Lanka",
  },
};

createPayHerePayment(paymentData)
  .then((result) => {
    // Initialize PayHere payment with result.payHereData
    initiatePayHerePayment(result.payHereData);
  })
  .catch((error) => {
    alert("Failed to create payment: " + error.message);
  });
```

#### Response Structure

```javascript
{
  "status": "success",
  "message": "PayHere payment data generated successfully",
  "orderId": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "amount": 75.00,
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
    "hash": "generated_hash_value",
    "sandbox": true
  }
}
```

### 2. PayHere Integration

Use the `payHereData` from the response to initialize PayHere payment:

```javascript
const initiatePayHerePayment = (payHereData) => {
  // Create PayHere payment object
  const payment = {
    sandbox: payHereData.sandbox,
    merchant_id: payHereData.merchant_id,
    return_url: payHereData.return_url,
    cancel_url: payHereData.cancel_url,
    notify_url: payHereData.notify_url,
    order_id: payHereData.order_id,
    items: payHereData.items,
    amount: payHereData.amount,
    currency: payHereData.currency,
    hash: payHereData.hash,
    first_name: payHereData.first_name,
    last_name: payHereData.last_name,
    email: payHereData.email,
    phone: payHereData.phone,
    address: payHereData.address,
    city: payHereData.city,
    country: payHereData.country,
  };

  // Show PayHere payment popup
  payhere.startPayment(payment);
};

// PayHere event handlers
payhere.onCompleted = function onCompleted(orderId) {
  console.log("Payment completed. OrderID:" + orderId);
  // Check payment status
  checkPaymentStatus(orderId);
};

payhere.onDismissed = function onDismissed() {
  console.log("Payment dismissed");
};

payhere.onError = function onError(error) {
  console.log("Error:" + error);
};
```

### 3. Check Payment Status

**GET** `/payments/status/{orderId}`

#### JavaScript Implementation

```javascript
const checkPaymentStatus = async (orderId) => {
  try {
    const response = await fetch(
      `http://localhost:8088/api/v1/payments/status/${orderId}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
      }
    );

    if (response.ok) {
      const result = await response.json();
      console.log("Payment status:", result);
      return result;
    } else if (response.status === 404) {
      console.log("Payment not found");
      return null;
    } else {
      throw new Error("Failed to check payment status");
    }
  } catch (error) {
    console.error("Error checking payment status:", error);
    throw error;
  }
};

// Usage
checkPaymentStatus("ORDER_cbe3e879-996e-4249-bcf6-527814b2b756")
  .then((result) => {
    if (result) {
      switch (result.status) {
        case "success":
          console.log("Payment successful!");
          // Redirect to success page
          window.location.href = "/payment/success";
          break;
        case "pending":
          console.log("Payment pending...");
          // Show pending message
          break;
        case "failure":
          console.log("Payment failed");
          // Show error message
          break;
        default:
          console.log("Unknown payment status");
      }
    } else {
      console.log("Payment not found");
    }
  })
  .catch((error) => {
    console.error("Error:", error);
  });
```

#### Response Structure

```javascript
{
  "status": "success",
  "message": "Payment status retrieved successfully",
  "orderId": "ORDER_cbe3e879-996e-4249-bcf6-527814b2b756",
  "amount": 75.00,
  "currency": "LKR",
  "timestamp": "2025-07-15T10:35:00"
}
```

### 4. Health Check

**GET** `/payments/health`

#### JavaScript Implementation

```javascript
const checkServiceHealth = async () => {
  try {
    const response = await fetch(
      "http://localhost:8088/api/v1/payments/health"
    );
    const result = await response.json();

    if (result.status === "UP") {
      console.log("Payment service is healthy");
      return true;
    } else {
      console.log("Payment service is down");
      return false;
    }
  } catch (error) {
    console.error("Error checking service health:", error);
    return false;
  }
};
```

## Complete Frontend Integration Example

### HTML

```html
<!DOCTYPE html>
<html>
  <head>
    <title>PayHere Payment Integration</title>
    <script src="https://www.payhere.lk/lib/payhere.js"></script>
  </head>
  <body>
    <div id="payment-form">
      <h2>Trip Booking Payment</h2>
      <form id="booking-form">
        <input type="text" id="firstName" placeholder="First Name" required />
        <input type="text" id="lastName" placeholder="Last Name" required />
        <input type="email" id="email" placeholder="Email" required />
        <input type="tel" id="phone" placeholder="Phone" required />
        <input type="text" id="address" placeholder="Address" required />
        <input type="text" id="city" placeholder="City" required />
        <input type="text" id="country" placeholder="Country" required />
        <input type="number" id="amount" placeholder="Amount" required />
        <button type="submit">Pay Now</button>
      </form>
    </div>

    <script src="payment-integration.js"></script>
  </body>
</html>
```

### JavaScript (payment-integration.js)

```javascript
class PaymentIntegration {
  constructor() {
    this.baseUrl = "http://localhost:8088/api/v1";
    this.initializeEventHandlers();
  }

  initializeEventHandlers() {
    document
      .getElementById("booking-form")
      .addEventListener("submit", this.handlePayment.bind(this));

    // PayHere event handlers
    payhere.onCompleted = this.onPaymentCompleted.bind(this);
    payhere.onDismissed = this.onPaymentDismissed.bind(this);
    payhere.onError = this.onPaymentError.bind(this);
  }

  async handlePayment(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const paymentData = {
      amount: parseFloat(formData.get("amount")),
      currency: "LKR",
      orderId: "ORDER_" + Date.now(),
      itemName: "Trip Booking",
      customerDetails: {
        firstName: formData.get("firstName"),
        lastName: formData.get("lastName"),
        email: formData.get("email"),
        phone: formData.get("phone"),
        address: formData.get("address"),
        city: formData.get("city"),
        country: formData.get("country"),
      },
    };

    try {
      const response = await this.createPayHerePayment(paymentData);
      this.initiatePayHerePayment(response.payHereData);
    } catch (error) {
      alert("Payment creation failed: " + error.message);
    }
  }

  async createPayHerePayment(paymentData) {
    const response = await fetch(
      `${this.baseUrl}/payments/create-payhere-payment`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(paymentData),
      }
    );

    const result = await response.json();

    if (!response.ok || result.status !== "success") {
      throw new Error(result.message || "Payment creation failed");
    }

    return result;
  }

  initiatePayHerePayment(payHereData) {
    const payment = {
      sandbox: payHereData.sandbox,
      merchant_id: payHereData.merchant_id,
      return_url: payHereData.return_url,
      cancel_url: payHereData.cancel_url,
      notify_url: payHereData.notify_url,
      order_id: payHereData.order_id,
      items: payHereData.items,
      amount: payHereData.amount,
      currency: payHereData.currency,
      hash: payHereData.hash,
      first_name: payHereData.first_name,
      last_name: payHereData.last_name,
      email: payHereData.email,
      phone: payHereData.phone,
      address: payHereData.address,
      city: payHereData.city,
      country: payHereData.country,
    };

    payhere.startPayment(payment);
  }

  async onPaymentCompleted(orderId) {
    console.log("Payment completed. OrderID:", orderId);

    try {
      const status = await this.checkPaymentStatus(orderId);
      if (status && status.status === "success") {
        alert("Payment successful!");
        window.location.href = "/payment/success";
      } else {
        alert("Payment status verification failed");
      }
    } catch (error) {
      console.error("Error verifying payment:", error);
      alert("Payment completed but verification failed");
    }
  }

  onPaymentDismissed() {
    console.log("Payment dismissed");
    alert("Payment was cancelled");
  }

  onPaymentError(error) {
    console.log("Payment error:", error);
    alert("Payment failed: " + error);
  }

  async checkPaymentStatus(orderId) {
    const response = await fetch(`${this.baseUrl}/payments/status/${orderId}`, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    });

    if (response.ok) {
      return await response.json();
    } else if (response.status === 404) {
      return null;
    } else {
      throw new Error("Failed to check payment status");
    }
  }
}

// Initialize payment integration
const paymentIntegration = new PaymentIntegration();
```

## Error Handling

### Common Error Responses

```javascript
// Validation Error (400)
{
  "status": "error",
  "message": "Validation failed: Amount is required",
  "orderId": "ORDER_123",
  "timestamp": "2025-07-15T10:30:00"
}

// Payment Not Found (404)
{
  "status": "error",
  "message": "Payment status not found",
  "orderId": "ORDER_123",
  "timestamp": "2025-07-15T10:30:00"
}

// Server Error (500)
{
  "status": "error",
  "message": "Internal server error: Connection failed",
  "orderId": "ORDER_123",
  "timestamp": "2025-07-15T10:30:00"
}
```

### Error Handling Best Practices

```javascript
const handleApiError = (error, orderId) => {
  console.error("API Error:", error);

  if (error.status === 400) {
    alert("Invalid payment data: " + error.message);
  } else if (error.status === 404) {
    alert("Payment not found");
  } else if (error.status === 500) {
    alert("Server error. Please try again later.");
  } else {
    alert("Unknown error occurred");
  }
};
```

## Testing

### Test Payment Data

```javascript
const testPaymentData = {
  amount: 100.0,
  currency: "LKR",
  orderId: "ORDER_TEST_" + Date.now(),
  itemName: "Test Trip Booking",
  customerDetails: {
    firstName: "John",
    lastName: "Doe",
    email: "john.doe@example.com",
    phone: "0771234567",
    address: "123 Test Street",
    city: "Colombo",
    country: "Sri Lanka",
  },
};
```

### Test PayHere Cards (Sandbox)

- **Visa**: 4916217501611292
- **MasterCard**: 5301250070000191
- **American Express**: 345678901234564

Use any future date for expiry and any 3-digit CVV.

## Security Considerations

1. **HTTPS**: Always use HTTPS in production
2. **CORS**: Configure CORS properly for your domain
3. **Input Validation**: Validate all inputs on the frontend
4. **Amount Verification**: Always verify payment amounts
5. **Order ID**: Use unique, non-guessable order IDs

## Support

For integration support, please refer to the main README.md or contact the development team.
