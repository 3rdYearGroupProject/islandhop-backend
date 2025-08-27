# MongoDB Integration Test Guide

## Overview

The PayHere Payment Service now saves payment details to MongoDB **immediately** when a payment is created, not just when notifications are received.

## What's Changed

### 1. Immediate Database Saves

- **Before**: Payment details only saved when PayHere sends notification
- **After**: Payment details saved immediately when payment is created, then updated when notification is received

### 2. New Flow

1. **Payment Creation** → Save to `payment_details` with status "PENDING"
2. **PayHere Notification** → Update payment details and move trip to `payed_trips_advance` (if successful)

## Testing the Integration

### 1. Test Payment Creation (Already Working)

Your order `ORDER_1756124398684` should now be saved in the database immediately.

### 2. Check if Payment Exists in Database

**New Endpoint**: `GET /api/v1/payments/check-payment/{orderId}`

```javascript
// Check if your recent payment is in the database
const response = await fetch(
  "http://localhost:8088/api/v1/payments/check-payment/ORDER_1756124398684"
);
const result = await response.json();
console.log(result);
```

**Expected Response:**

```json
{
  "status": "success",
  "exists": true,
  "paymentStatus": "PENDING",
  "amount": 18113.7,
  "currency": "LKR",
  "createdAt": "2025-08-25T12:24:36.000Z",
  "paymentDate": null
}
```

### 3. Simulate PayHere Notification (For Testing)

```bash
curl -X POST http://localhost:8088/api/v1/payments/notify-form \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "order_id=ORDER_1756124398684&status_code=2&status_message=Success&payment_id=220348759&amount=18113.70&currency=LKR&merchant_id=1231228&payhere_amount=18113.70&payhere_currency=LKR&md5sig=GENERATED_HASH"
```

## Database Collections

### payment_details Collection

```javascript
{
  "_id": "generated-id",
  "orderId": "ORDER_1756124398684",
  "tripId": null,  // Will be null for non-trip orders
  "userId": null,  // Will be null if trip ID can't be extracted
  "amount": 18113.7,
  "currency": "LKR",
  "paymentMethod": "PayHere",
  "paymentStatus": "PENDING",  // Changes to SUCCESS/FAILED after notification
  "payHereTransactionId": null,  // Set after notification
  "payHereStatusCode": null,     // Set after notification
  "payHereStatusMessage": null,  // Set after notification
  "paymentDate": null,           // Set after successful notification
  "createdAt": "2025-08-25T12:24:36.000Z",
  "lastUpdated": "2025-08-25T12:24:36.000Z"
}
```

### payed_trips_advance Collection

Only created when:

1. Payment is successful (status_code = 2)
2. Order ID follows format: `trip_{tripId}_payment`
3. Trip exists in `initiated_trips` collection

## Order ID Formats

### Current Format (Your Frontend)

- `ORDER_1756124398684`
- ✅ **Saves payment details**
- ❌ **Cannot extract trip ID** (no trip migration)

### Expected Format for Trip Integration

- `trip_042cea55-afae-4c2e-9d19-e3c40490579b_payment`
- ✅ **Saves payment details**
- ✅ **Extracts trip ID and migrates trip data**

## Recommendations

### Option 1: Keep Current Format (Simpler)

- Continue using `ORDER_` format
- Payment details will be saved to database
- Trip data won't be automatically migrated
- Manual trip association can be done later if needed

### Option 2: Update to Trip Format (Full Integration)

- Change frontend to use `trip_{tripId}_payment` format
- Full integration with trip data
- Automatic trip migration to paid collection

## Testing Commands

### 1. Check Payment Exists

```bash
curl http://localhost:8088/api/v1/payments/check-payment/ORDER_1756124398684
```

### 2. Get Payment Details

```bash
curl http://localhost:8088/api/v1/payments/payment-details/ORDER_1756124398684
```

### 3. Check Service Health

```bash
curl http://localhost:8088/api/v1/payments/health
```

## Frontend Integration Update

Add this to your payment success handler:

```javascript
// After payment completion
const checkPaymentInDB = async (orderId) => {
  try {
    const response = await fetch(`/api/v1/payments/check-payment/${orderId}`);
    const result = await response.json();

    if (result.exists) {
      console.log("✅ Payment saved to database:", result);
    } else {
      console.log("❌ Payment not found in database");
    }
  } catch (error) {
    console.error("Error checking payment in database:", error);
  }
};

// Call this after payment completion
checkPaymentInDB("ORDER_1756124398684");
```

## Troubleshooting

### Payment Not Saved

1. Check service logs for errors
2. Verify MongoDB connection
3. Check if collections have proper permissions

### Trip Not Migrated

1. Verify order ID format: `trip_{tripId}_payment`
2. Check if trip exists in `initiated_trips` collection
3. Verify PayHere notification was received

### Database Connection Issues

1. Check MongoDB Atlas connection string
2. Verify network access (IP whitelist)
3. Check credentials and database name

## Success Indicators

✅ **Payment Details Saved**: Check using `/check-payment/{orderId}`
✅ **MongoDB Connected**: Service starts without errors
✅ **Collections Created**: `payment_details` appears in MongoDB
✅ **Data Persisted**: Payment details survive service restart

Your payment `ORDER_1756124398684` should now be in the database!
