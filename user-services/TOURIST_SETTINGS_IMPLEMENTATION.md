# Tourist Settings Implementation Summary

## Overview

This implementation adds tourist settings functionality to store user preferences for currency and units in a new `tourist_settings` table.

## Files Created/Modified

### 1. Database Schema

**File:** `create_tourist_settings_table.sql`

- Creates `tourist_settings` table with email, currency, units columns
- Includes UUID primary key, timestamps, and email index

### 2. Entity Model

**File:** `TouristSettings.java`

- JPA entity for tourist_settings table
- Includes automatic timestamp management
- Default values: currency="USD", units="Imperial"

### 3. Repository

**File:** `TouristSettingsRepository.java`

- JPA repository interface
- Methods: findByEmail(), existsByEmail(), deleteByEmail()

### 4. Controller Endpoints

**Modified:** `TouristController.java`

- Added `TouristSettingsRepository` dependency
- Added GET `/tourist/settings` endpoint
- Added PUT `/tourist/settings` endpoint

### 5. Documentation

**File:** `frontend_integration.md`

- Complete frontend integration guide
- JavaScript examples for React/axios
- Error handling patterns

### 6. Testing

**File:** `test-settings-api.ps1`

- PowerShell script to test all endpoints
- JavaScript examples included

## API Endpoints

### GET /tourist/settings

- **Purpose:** Retrieve user settings
- **Authentication:** Session-based or email parameter
- **Response:** Returns settings object or null values if none exist
- **Example Response (no settings):**

```json
{
  "email": "user@example.com",
  "currency": null,
  "units": null
}
```

### PUT /tourist/settings

- **Purpose:** Create or update user settings
- **Authentication:** Session-based or email in body
- **Behavior:** INSERT if no row exists, UPDATE if row exists
- **Request Body:**

```json
{
  "email": "user@example.com", // Optional if using session
  "currency": "EUR", // Optional
  "units": "Metric" // Optional
}
```

## Database Table Structure

```sql
CREATE TABLE tourist_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    currency VARCHAR(10) DEFAULT 'USD',
    units VARCHAR(20) DEFAULT 'Imperial',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Frontend Integration (React Example)

```javascript
// Get settings
const settings = await api.get("/tourist/settings");

// Update settings
await api.put("/tourist/settings", {
  currency: "EUR",
  units: "Metric",
});
```

## Key Features

✅ **Upsert Functionality:** Automatically creates new record or updates existing one
✅ **Null Handling:** Returns null values when no settings exist (as requested)
✅ **Session Support:** Works with both session-based and explicit email authentication
✅ **Partial Updates:** Can update just currency or just units
✅ **Validation:** Checks for tourist account existence
✅ **Logging:** Comprehensive logging for debugging
✅ **Error Handling:** Proper HTTP status codes and error messages

## Next Steps

1. Run the SQL script in your cloud PostgreSQL database
2. Restart your Spring Boot application
3. Test the endpoints using the provided PowerShell script
4. Update your frontend to use the new settings endpoints

## Testing

Run the test script:

```powershell
.\test-settings-api.ps1
```

The implementation is complete and ready for use!
