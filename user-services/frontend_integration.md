# User Services Frontend Integration Guide

This document outlines how to integrate with the IslandHop User Services API endpoints from the frontend.

## Base URL

```
http://localhost:8083/api/v1/tourist
```

## Authentication

All endpoints use session-based authentication. Include credentials in requests:

```javascript
// Configure axios for session-based auth
const api = axios.create({
  baseURL: "http://localhost:8083/api/v1",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});
```

---

## Tourist Settings Endpoints

### GET /tourist/settings

Retrieves user settings (currency, units). Returns null values if no settings exist.

**Request:**

```javascript
// Get settings for current user (uses session)
const getSettings = async () => {
  try {
    const response = await api.get("/tourist/settings");
    return response.data;
  } catch (error) {
    console.error("Error fetching settings:", error);
    throw error;
  }
};

// Get settings for specific user (with email parameter)
const getSettingsForUser = async (email) => {
  try {
    const response = await api.get(`/tourist/settings?email=${email}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching settings:", error);
    throw error;
  }
};
```

**Response (when settings exist):**

```json
{
  "email": "user@example.com",
  "currency": "USD",
  "units": "Imperial"
}
```

**Response (when no settings exist):**

```json
{
  "email": "user@example.com",
  "currency": null,
  "units": null
}
```

**Error Responses:**

- `401 Unauthorized`: Not authenticated
- `400 Bad Request`: Tourist account does not exist

---

### PUT /tourist/settings

Updates or creates user settings. If no settings exist for the email, creates new record.

**Request:**

```javascript
const updateSettings = async (settingsData) => {
  try {
    const response = await api.put("/tourist/settings", {
      email: settingsData.email, // Optional if using session
      currency: settingsData.currency,
      units: settingsData.units,
    });
    return response.data;
  } catch (error) {
    console.error("Error updating settings:", error);
    throw error;
  }
};

// Example usage
const handleSaveSettings = async () => {
  const settingsData = {
    currency: "EUR",
    units: "Metric",
  };

  try {
    const result = await updateSettings(settingsData);
    console.log("Settings saved:", result);
  } catch (error) {
    console.error("Failed to save settings:", error);
  }
};
```

**Request Body:**

```json
{
  "email": "user@example.com", // Optional if using session
  "currency": "EUR", // Optional
  "units": "Metric" // Optional
}
```

**Response:**

```json
{
  "email": "user@example.com",
  "currency": "EUR",
  "units": "Metric",
  "message": "Settings updated successfully"
}
```

**Error Responses:**

- `401 Unauthorized`: Not authenticated
- `400 Bad Request`: Tourist account does not exist
- `500 Internal Server Error`: Database or server error

---

## Tourist Profile Endpoints

### GET /tourist/profile

Retrieves user profile information including personal details and preferences.

**Request:**

```javascript
// Get profile for current user (uses session)
const getProfile = async () => {
  try {
    const response = await api.get("/tourist/profile");
    return response.data;
  } catch (error) {
    console.error("Error fetching profile:", error);
    throw error;
  }
};
```

**Response:**

```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-05-15",
  "nationality": "Sri Lanka",
  "languages": ["English", "සිංහල"],
  "profilePic": [
    /* byte array */
  ],
  "profileCompletion": 1
}
```

**Error Responses:**

- `401 Unauthorized`: Not authenticated
- `400 Bad Request`: Tourist account does not exist

---

### PUT /tourist/profile

Updates user profile information. Supports partial updates.

**Request:**

```javascript
const updateProfile = async (profileData) => {
  try {
    const response = await api.put("/tourist/profile", {
      firstName: profileData.firstName,
      lastName: profileData.lastName,
      dob: profileData.dob, // ISO date string (YYYY-MM-DD)
      nationality: profileData.nationality,
      languages: profileData.languages,
      profilePicture: profileData.profilePictureByteArray, // byte array
    });
    return response.data;
  } catch (error) {
    console.error("Error updating profile:", error);
    throw error;
  }
};
```

**Request Body:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-05-15",
  "nationality": "Sri Lanka",
  "languages": ["English", "සිංහල"],
  "profilePicture": [
    /* byte array */
  ]
}
```

**Response:**

```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-05-15",
  "nationality": "Sri Lanka",
  "languages": ["English", "සිංහල"],
  "profilePic": [
    /* byte array */
  ],
  "profileCompletion": 1,
  "message": "Profile updated successfully"
}
```

**Error Responses:**

- `401 Unauthorized`: Not authenticated
- `400 Bad Request`: Invalid input data
- `500 Internal Server Error`: Database or server error

---

## Integration Example with React Component

```javascript
import React, { useState, useEffect } from "react";
import api from "../api/axios";

const SettingsComponent = () => {
  const [settings, setSettings] = useState({
    currency: "USD",
    units: "Imperial",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // Fetch settings on component mount
  useEffect(() => {
    const fetchSettings = async () => {
      setLoading(true);
      try {
        const response = await api.get("/tourist/settings");
        // Handle null values by using defaults
        setSettings({
          currency: response.data.currency || "USD",
          units: response.data.units || "Imperial",
        });
      } catch (err) {
        setError("Failed to load settings");
      } finally {
        setLoading(false);
      }
    };

    fetchSettings();
  }, []);

  // Save settings
  const handleSaveSettings = async () => {
    setLoading(true);
    setError("");

    try {
      await api.put("/tourist/settings", settings);
      // Settings saved successfully
    } catch (err) {
      setError("Failed to save settings");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Your settings UI here */}
      <select
        value={settings.currency}
        onChange={(e) => setSettings({ ...settings, currency: e.target.value })}
      >
        <option value="USD">USD</option>
        <option value="EUR">EUR</option>
        {/* More options */}
      </select>

      <select
        value={settings.units}
        onChange={(e) => setSettings({ ...settings, units: e.target.value })}
      >
        <option value="Imperial">Imperial</option>
        <option value="Metric">Metric</option>
      </select>

      <button onClick={handleSaveSettings} disabled={loading}>
        Save Settings
      </button>

      {error && <div className="error">{error}</div>}
    </div>
  );
};

export default SettingsComponent;
```

---

## Available Currency Options

The frontend should support these currency codes:

```javascript
const CURRENCIES = [
  "USD",
  "EUR",
  "GBP",
  "JPY",
  "AUD",
  "CAD",
  "CHF",
  "CNY",
  "SEK",
  "NZD",
  "MXN",
  "SGD",
  "HKD",
  "NOK",
  "ZAR",
  "THB",
  "BRL",
  "INR",
  "RUB",
  "KRW",
  "LKR",
  "PKR",
  "BDT",
  "NPR",
  "MVR",
  "IDR",
  "MYR",
  "PHP",
  "VND",
  // ... more currencies
];
```

## Available Units Options

```javascript
const UNITS = ["Imperial", "Metric"];
```

---

## Error Handling Best Practices

```javascript
const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;
    switch (status) {
      case 401:
        // Redirect to login
        window.location.href = "/login";
        break;
      case 400:
        console.error("Bad request:", data);
        break;
      case 500:
        console.error("Server error:", data);
        break;
      default:
        console.error("Unexpected error:", data);
    }
  } else if (error.request) {
    // Network error
    console.error("Network error:", error.request);
  } else {
    // Other error
    console.error("Error:", error.message);
  }
};
```

---

## Tourist Null Value Checker Endpoint

### GET /tourist/check-null-values

Checks for null values across all tourist-related tables. Useful for debugging data integrity issues and identifying incomplete profiles.

**Request:**

```javascript
// Check null values for current user (uses session)
const checkNullValues = async () => {
  try {
    const response = await api.get("/tourist/check-null-values");
    return response.data;
  } catch (error) {
    console.error("Error checking null values:", error);
    throw error;
  }
};

// Check null values for specific user (with email parameter)
const checkNullValuesForUser = async (email) => {
  try {
    const response = await api.get(`/tourist/check-null-values?email=${email}`);
    return response.data;
  } catch (error) {
    console.error("Error checking null values:", error);
    throw error;
  }
};
```

**Response:**

```json
{
  "email": "user@example.com",
  "nullFields": {
    "tourist_accounts": {
      "status": "null"
    },
    "tourist_profiles": {
      "firstName": "null",
      "lastName": "null",
      "dob": "null",
      "nationality": "null",
      "languages": "null",
      "profilePic": "null"
    },
    "tourist_profile_languages": {
      "languages": "empty/null"
    },
    "tourist_settings": {
      "entire_settings": "null"
    }
  },
  "message": "Null value check completed"
}
```

**Usage Examples:**

```javascript
// Check for profile completion issues
const validateProfile = async () => {
  const nullCheck = await checkNullValues();

  if (nullCheck.nullFields.tourist_profiles) {
    const missingFields = Object.keys(nullCheck.nullFields.tourist_profiles);
    console.log("Missing profile fields:", missingFields);

    // Guide user to complete profile
    if (
      missingFields.includes("firstName") ||
      missingFields.includes("lastName")
    ) {
      showProfileCompletionModal();
    }
  }
};

// Check if settings need initialization
const initializeUserSettings = async () => {
  const nullCheck = await checkNullValues();

  if (nullCheck.nullFields.tourist_settings?.entire_settings === "null") {
    console.log("User needs settings initialized");
    // Create default settings
    await updateSettings({
      currency: "USD",
      units: "Imperial",
    });
  }
};

// Validate data integrity before critical operations
const validateDataIntegrity = async () => {
  const nullCheck = await checkNullValues();

  if (Object.keys(nullCheck.nullFields).length > 0) {
    console.warn("Data integrity issues found:", nullCheck.nullFields);
    return false;
  }

  return true;
};
```

---

## Tourist Settings Endpoints (Continued)

### GET /tourist/settings (Continued)

// Get settings for specific user (with email parameter)
const getSettingsForUser = async (email) => {
try {
const response = await api.get(`/tourist/settings?email=${email}`);
return response.data;
} catch (error) {
console.error("Error fetching settings:", error);
throw error;
}
};
