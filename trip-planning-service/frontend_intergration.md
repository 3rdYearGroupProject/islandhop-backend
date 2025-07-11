## 🎯 Available Endpoints

### 1. POST /itinerary - Create New Trip Itinerary

**Full Endpoint:** `POST http://localhost:8084/api/itinerary`

**Purpose:** Initializes a new trip plan with user preferences and creates empty daily plans for the date range.

**🔥 CRITICAL FOR AI: This endpoint is the STARTING POINT for all trip planning workflows.**

#### 📋 Request Schema (EXACT FORMAT REQUIRED)

```typescript
interface CreateTripRequest {
    // REQUIRED FIELDS - Must be provided
    userId: string;              // User identifier (e.g., "user_789")
    tripName: string;            // Trip name (e.g., "Sri Lanka Adventure")
    startDate: string;           // ISO date format YYYY-MM-DD (e.g., "2025-08-10")
    endDate: string;             // ISO date format YYYY-MM-DD (e.g., "2025-08-15")
    baseCity: string;            // Starting city (e.g., "Colombo")
    
    // OPTIONAL FIELDS - Include for better trip planning
    arrivalTime?: string;        // HH:mm format (e.g., "21:30") or empty string ""
    multiCityAllowed?: boolean;  // Default: true
    activityPacing?: "Relaxed" | "Normal" | "Fast";  // Default: "Normal"
    budgetLevel?: "Low" | "Medium" | "High";         // Default: "Medium"
    preferredTerrains?: string[];     // Array of terrain preferences
    preferredActivities?: string[];   // Array of activity preferences
}
```

#### 🎯 Response Schema (WHAT YOU'LL RECEIVE)

```typescript
// SUCCESS RESPONSE (HTTP 201)
interface CreateTripSuccessResponse {
    status: "success";
    tripId: string;              // UUID format (e.g., "335ace95-73c1-4bd2-9d69-fd23fb27d9d2")
    message: "Trip created successfully";
}

// ERROR RESPONSE (HTTP 400 - Validation Error)
interface CreateTripValidationError {
    status: "error";
    message: "Validation failed";
    errors: {
        [fieldName: string]: string;  // Field-specific error messages
    };
}

// ERROR RESPONSE (HTTP 500 - Server Error)
interface CreateTripServerError {
    status: "error";
    tripId: null;
    message: "Internal server error";
}
```

#### 🔧 Complete JavaScript Implementation

**1. PRODUCTION-READY FETCH IMPLEMENTATION**

```javascript
/**
 * Creates a new trip itinerary
 * @param {CreateTripRequest} tripData - The trip data
 * @returns {Promise<CreateTripSuccessResponse>} - The created trip response
 * @throws {Error} - Throws error with descriptive message on failure
 */
const createTripItinerary = async (tripData) => {
    // Input validation
    if (!tripData.userId?.trim()) {
        throw new Error('User ID is required');
    }
    if (!tripData.tripName?.trim()) {
        throw new Error('Trip name is required');
    }
    if (!tripData.startDate) {
        throw new Error('Start date is required');
    }
    if (!tripData.endDate) {
        throw new Error('End date is required');
    }
    if (!tripData.baseCity?.trim()) {
        throw new Error('Base city is required');
    }

    // Date validation
    const startDate = new Date(tripData.startDate);
    const endDate = new Date(tripData.endDate);
    if (startDate > endDate) {
        throw new Error('Start date must be before or equal to end date');
    }

    // Prepare request payload with defaults
    const requestPayload = {
        userId: tripData.userId.trim(),
        tripName: tripData.tripName.trim(),
        startDate: tripData.startDate,
        endDate: tripData.endDate,
        baseCity: tripData.baseCity.trim(),
        arrivalTime: tripData.arrivalTime || "",
        multiCityAllowed: tripData.multiCityAllowed ?? true,
        activityPacing: tripData.activityPacing || "Normal",
        budgetLevel: tripData.budgetLevel || "Medium",
        preferredTerrains: tripData.preferredTerrains || [],
        preferredActivities: tripData.preferredActivities || []
    };

    try {
        console.log('🚀 Creating trip itinerary...', requestPayload);
        
        const response = await fetch('http://localhost:8084/api/itinerary', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include', // CORS: Include credentials if needed
            body: JSON.stringify(requestPayload)
        });

        const responseData = await response.json();
        
        if (response.status === 201) {
            console.log('✅ Trip created successfully:', responseData);
            return responseData;
        } else if (response.status === 400) {
            console.error('❌ Validation error:', responseData);
            const errorMessages = responseData.errors 
                ? Object.values(responseData.errors).join(', ')
                : responseData.message;
            throw new Error(`Validation failed: ${errorMessages}`);
        } else if (response.status === 500) {
            console.error('💥 Server error:', responseData);
            throw new Error('Server error: Please try again later');
        } else {
            console.error('🔴 Unexpected error:', responseData);
            throw new Error(responseData.message || 'Unexpected error occurred');
        }
    } catch (networkError) {
        if (networkError.name === 'TypeError') {
            console.error('🌐 Network/CORS error:', networkError);
            throw new Error('Network or CORS error: Please check your connection and CORS configuration');
        }
        throw networkError;
    }
};
```

**2. AXIOS IMPLEMENTATION (CORS-Ready Alternative)**

```javascript
import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8084/api',
    timeout: 30000,
    withCredentials: true, // CORS: Enable credentials if needed
    headers: {
        'Content-Type': 'application/json'
    }
});

const createTripWithAxios = async (tripData) => {
    try {
        const response = await axiosClient.post('/itinerary', {
            userId: tripData.userId,
            tripName: tripData.tripName,
            startDate: tripData.startDate,
            endDate: tripData.endDate,
            baseCity: tripData.baseCity,
            arrivalTime: tripData.arrivalTime || "",
            multiCityAllowed: tripData.multiCityAllowed ?? true,
            activityPacing: tripData.activityPacing || "Normal",
            budgetLevel: tripData.budgetLevel || "Medium",
            preferredTerrains: tripData.preferredTerrains || [],
            preferredActivities: tripData.preferredActivities || []
        });

        return response.data;
    } catch (error) {
        if (error.response) {
            // Server responded with error status
            const { status, data } = error.response;
            if (status === 400) {
                const errorMsg = data.errors 
                    ? Object.values(data.errors).join(', ')
                    : data.message;
                throw new Error(`Validation failed: ${errorMsg}`);
            } else if (status === 500) {
                throw new Error('Server error: Please try again later');
            }
            throw new Error(data.message || 'Request failed');
        } else if (error.request) {
            // Network error
            throw new Error('Network error: Please check your connection');
        } else {
            throw new Error(error.message);
        }
    }
};
```

#### 🧪 Testing Examples & Use Cases

**EXAMPLE 1: Complete Trip (All Fields)**
```javascript
const fullTripExample = {
    userId: "user_789",
    tripName: "Sri Lanka Cultural Explorer",
    startDate: "2025-08-10",
    endDate: "2025-08-15",
    arrivalTime: "21:30",
    baseCity: "Colombo",
    multiCityAllowed: true,
    activityPacing: "Normal",
    budgetLevel: "Medium",
    preferredTerrains: ["Beach", "Mountain", "Historical", "National Park"],
    preferredActivities: ["Hiking", "Cultural Tours", "Wildlife Safari", "Photography"]
};

// Execute
createTripItinerary(fullTripExample)
    .then(result => {
        console.log('🎯 Trip ID:', result.tripId);
        console.log('📝 Status:', result.status);
        console.log('💬 Message:', result.message);
        // Expected: { status: "success", tripId: "uuid-here", message: "Trip created successfully" }
    })
    .catch(error => console.error('❌ Error:', error.message));
```

**EXAMPLE 2: Minimal Trip (Required Fields Only)**
```javascript
const minimalTripExample = {
    userId: "user_123",
    tripName: "Weekend Getaway",
    startDate: "2025-08-20",
    endDate: "2025-08-22",
    baseCity: "Kandy"
    // All optional fields will use defaults
};

createTripItinerary(minimalTripExample)
    .then(result => {
        // Will receive same success response structure
        console.log('🎯 Minimal trip created:', result.tripId);
    });
```

**EXAMPLE 3: Error Handling Demo (Invalid Data)**
```javascript
const invalidTripExample = {
    userId: "",  // Empty - will cause validation error
    tripName: "Invalid Trip",
    startDate: "2025-08-20",
    endDate: "2025-08-15",  // End before start - validation error
    baseCity: "Galle"
};

createTripItinerary(invalidTripExample)
    .then(result => {
        // This won't execute
    })
    .catch(error => {
        console.log('⚠️ Expected validation error:', error.message);
        // Expected: "Validation failed: User ID is required, Start date must be before or equal to end date"
    });
```

#### 🔍 Advanced Validation & Error Handling

**CLIENT-SIDE VALIDATION FUNCTION**
```javascript
/**
 * Validates trip data before sending to API
 * @param {Object} tripData - The trip data to validate
 * @returns {Object} - { isValid: boolean, errors: string[] }
 */
const validateTripData = (tripData) => {
    const errors = [];

    // Required field validation
    if (!tripData.userId?.trim()) errors.push('User ID is required');
    if (!tripData.tripName?.trim()) errors.push('Trip name is required');
    if (!tripData.startDate) errors.push('Start date is required');
    if (!tripData.endDate) errors.push('End date is required');
    if (!tripData.baseCity?.trim()) errors.push('Base city is required');

    // Date validation
    if (tripData.startDate && tripData.endDate) {
        const start = new Date(tripData.startDate);
        const end = new Date(tripData.endDate);
        
        if (isNaN(start.getTime())) errors.push('Invalid start date format');
        if (isNaN(end.getTime())) errors.push('Invalid end date format');
        if (start > end) errors.push('Start date must be before or equal to end date');
        
        // Check if dates are in the future
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (start < today) errors.push('Start date cannot be in the past');
    }

    // Time validation (if provided)
    if (tripData.arrivalTime && tripData.arrivalTime !== "") {
        const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
        if (!timeRegex.test(tripData.arrivalTime)) {
            errors.push('Arrival time must be in HH:mm format (e.g., 14:30)');
        }
    }

    // Enum validation
    const validPacing = ["Relaxed", "Normal", "Fast"];
    if (tripData.activityPacing && !validPacing.includes(tripData.activityPacing)) {
        errors.push(`Activity pacing must be one of: ${validPacing.join(', ')}`);
    }

    const validBudget = ["Low", "Medium", "High"];
    if (tripData.budgetLevel && !validBudget.includes(tripData.budgetLevel)) {
        errors.push(`Budget level must be one of: ${validBudget.join(', ')}`);
    }

    // Array validation
    if (tripData.preferredTerrains && !Array.isArray(tripData.preferredTerrains)) {
        errors.push('Preferred terrains must be an array');
    }
    if (tripData.preferredActivities && !Array.isArray(tripData.preferredActivities)) {
        errors.push('Preferred activities must be an array');
    }

    return {
        isValid: errors.length === 0,
        errors
    };
};

// Usage with validation
const createTripWithValidation = async (tripData) => {
    // Validate first
    const validation = validateTripData(tripData);
    if (!validation.isValid) {
        throw new Error(`Validation failed: ${validation.errors.join(', ')}`);
    }

    // Proceed with API call
    return await createTripItinerary(tripData);
};
```

#### ⚛️ React Hook Implementation

```javascript
import { useState, useCallback } from 'react';

/**
 * Custom React hook for trip creation
 * @returns {Object} - { createTrip, loading, error, success, reset }
 */
const useTripCreation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const createTrip = useCallback(async (tripData) => {
        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            // Validate before sending
            const validation = validateTripData(tripData);
            if (!validation.isValid) {
                throw new Error(validation.errors.join(', '));
            }

            const result = await createTripItinerary(tripData);
            setSuccess(result);
            setLoading(false);
            return result;
        } catch (err) {
            setError(err.message);
            setLoading(false);
            throw err;
        }
    }, []);

    const reset = useCallback(() => {
        setLoading(false);
        setError(null);
        setSuccess(null);
    }, []);

    return { createTrip, loading, error, success, reset };
};

// Example React Component Usage
const TripCreationForm = () => {
    const { createTrip, loading, error, success, reset } = useTripCreation();
    const [formData, setFormData] = useState({
        userId: '',
        tripName: '',
        startDate: '',
        endDate: '',
        baseCity: '',
        arrivalTime: '',
        multiCityAllowed: true,
        activityPacing: 'Normal',
        budgetLevel: 'Medium',
        preferredTerrains: [],
        preferredActivities: []
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const result = await createTrip(formData);
            console.log('🎉 Trip created successfully:', result.tripId);
            // Reset form or redirect
            setFormData({ /* reset to initial state */ });
        } catch (error) {
            console.error('❌ Failed to create trip:', error);
            // Error is already set in hook state
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            {/* Form fields here */}
            
            {loading && <div className="loading">Creating trip...</div>}
            
            {error && (
                <div className="error">
                    <p>Error: {error}</p>
                    <button onClick={reset}>Dismiss</button>
                </div>
            )}
            
            {success && (
                <div className="success">
                    <p>Trip created successfully!</p>
                    <p>Trip ID: {success.tripId}</p>
                    <button onClick={reset}>Create Another Trip</button>
                </div>
            )}
            
            <button type="submit" disabled={loading}>
                {loading ? 'Creating...' : 'Create Trip'}
            </button>
        </form>
    );
};
```

#### 🔄 Promise-based Wrapper (For AI Systems)

```javascript
/**
 * Promise-based trip creation function optimized for AI integration
 * @param {CreateTripRequest} tripData - Trip data object
 * @returns {Promise<{success: boolean, data?: any, error?: string, tripId?: string}>}
 */
const createTripPromise = (tripData) => {
    return new Promise(async (resolve, reject) => {
        try {
            // Validate input
            const validation = validateTripData(tripData);
            if (!validation.isValid) {
                resolve({
                    success: false,
                    error: `Validation failed: ${validation.errors.join(', ')}`
                });
                return;
            }

            // Make API call
            const result = await createTripItinerary(tripData);
            
            resolve({
                success: true,
                data: result,
                tripId: result.tripId,
                message: result.message
            });
        } catch (error) {
            resolve({
                success: false,
                error: error.message,
                data: null
            });
        }
    });
};

// AI-friendly usage
const aiTripCreation = async (userInput) => {
    const tripData = {
        userId: userInput.userId,
        tripName: userInput.tripName,
        startDate: userInput.startDate,
        endDate: userInput.endDate,
        baseCity: userInput.baseCity,
        // ... other fields
    };

    const result = await createTripPromise(tripData);
    
    if (result.success) {
        return {
            status: 'success',
            tripId: result.tripId,
            message: 'Trip created successfully'
        };
    } else {
        return {
            status: 'error',
            message: result.error
        };
    }
};
```

## 🚨 Error Handling Guide

### HTTP Status Codes & Responses

| Status Code | Meaning | Response Structure | Action Required |
|------------|---------|-------------------|------------------|
| **201** | Created | `{status: "success", tripId: "uuid", message: "Trip created successfully"}` | ✅ Success - Store tripId |
| **400** | Bad Request | `{status: "error", message: "Validation failed", errors: {...}}` | ❌ Fix validation errors |
| **500** | Server Error | `{status: "error", tripId: null, message: "Internal server error"}` | 🔄 Retry request |
| **Network** | Connection Issue | Browser/Network error | 🌐 Check connection |

### 🎯 Error Response Examples

**Validation Error (400):**
```json
{
    "status": "error",
    "message": "Validation failed",
    "errors": {
        "userId": "User ID cannot be blank",
        "startDate": "Start date must be before or equal to end date",
        "baseCity": "Base city cannot be blank"
    }
}
```

**Server Error (500):**
```json
{
    "status": "error",
    "tripId": null,
    "message": "Internal server error"
}
```

### 🔧 Error Handling Implementation

```javascript
const handleTripCreationError = (error, response = null) => {
    if (response) {
        switch (response.status) {
            case 400:
                return {
                    type: 'validation',
                    message: 'Please fix the form errors',
                    details: response.data.errors,
                    retryable: false
                };
            case 500:
                return {
                    type: 'server',
                    message: 'Server error. Please try again.',
                    retryable: true
                };
            default:
                return {
                    type: 'unknown',
                    message: 'Unexpected error occurred',
                    retryable: true
                };
        }
    } else {
        // Network error
        return {
            type: 'network',
            message: 'Connection failed. Check your internet.',
            retryable: true
        };
    }
};
```

# Frontend Integration Guide - Trip Planning Service

## 🚀 AI Integration Ready Documentation

This guide provides complete integration details for AI systems to connect frontend applications to the IslandHop Trip Planning Service backend.

## 📡 Base Configuration & CORS Setup

```javascript
const API_CONFIG = {
    baseURL: 'http://localhost:8084/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};
```

### 🌐 CORS Configuration (Already Configured on Backend)

The backend is configured with CORS support for cross-origin requests:

**Allowed Origins:** `http://localhost:3000` (configurable via `cors.allowed-origins`)
**Allowed Methods:** `GET, POST, PUT, DELETE, OPTIONS`
**Allowed Headers:** `*` (all headers)
**Allow Credentials:** `true`
**Max Age:** `3600 seconds`

**Important for Frontend Developers:**
- ✅ No additional CORS configuration needed on frontend
- ✅ Requests from `localhost:3000` are automatically allowed
- ✅ All standard HTTP methods are supported
- ✅ Cookies and credentials can be sent if needed
- ✅ Preflight OPTIONS requests are handled automatically

**For Different Frontend Ports:**
If your frontend runs on a different port, update the backend configuration by setting:
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:3001,http://localhost:8080
```

### 🔧 Frontend Request Configuration

**Standard Fetch (CORS-ready):**
```javascript
const response = await fetch('http://localhost:8084/api/itinerary', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    credentials: 'include', // Enable if you need cookies/auth
    body: JSON.stringify(tripData)
});
```

**Axios Configuration (CORS-ready):**
```javascript
const axiosClient = axios.create({
    baseURL: 'http://localhost:8084/api',
    withCredentials: true, // Enable if you need cookies/auth
    headers: {
        'Content-Type': 'application/json'
    }
});
```

### 🛠️ CORS Troubleshooting Guide

#### Common CORS Issues & Solutions

**1. CORS Error from Different Port:**
```
Error: Access to fetch at 'http://localhost:8084/api/itinerary' from origin 'http://localhost:3001' has been blocked by CORS policy
```

**Solution:** Add your frontend port to the backend configuration:
```properties
# In application.yml or application.properties
cors.allowed-origins=http://localhost:3000,http://localhost:3001,http://localhost:8080
```

**2. CORS Error in Production:**
```
Error: Access to fetch at 'https://api.islandhop.com/api/itinerary' from origin 'https://app.islandhop.com' has been blocked by CORS policy
```

**Solution:** Update backend configuration for production domains:
```properties
cors.allowed-origins=https://app.islandhop.com,https://www.islandhop.com
```

**3. Preflight OPTIONS Request Failing:**
```
Error: Response to preflight request doesn't pass access control check
```

**Solution:** The backend already handles OPTIONS requests. Ensure your request includes proper headers:
```javascript
const response = await fetch('http://localhost:8084/api/itinerary', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json', // This triggers preflight
        'Accept': 'application/json'
    },
    body: JSON.stringify(tripData)
});
```

**4. Credentials Not Being Sent:**
```javascript
// Wrong: Credentials not included
fetch('http://localhost:8084/api/itinerary', {
    method: 'POST',
    // credentials missing
});

// Correct: Include credentials
fetch('http://localhost:8084/api/itinerary', {
    method: 'POST',
    credentials: 'include', // Enables cookies/auth headers
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(tripData)
});
```

#### CORS Testing Commands

**Test CORS with cURL:**
```bash
# Test preflight OPTIONS request
curl -X OPTIONS http://localhost:8084/api/itinerary \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# Test actual POST request
curl -X POST http://localhost:8084/api/itinerary \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -d '{"userId":"test","tripName":"Test","startDate":"2025-08-10","endDate":"2025-08-15","baseCity":"Colombo"}' \
  -v
```

**JavaScript CORS Test Function:**
```javascript
const testCORS = async () => {
    try {
        const response = await fetch('http://localhost:8084/api/itinerary', {
            method: 'OPTIONS',
            headers: {
                'Origin': window.location.origin,
                'Access-Control-Request-Method': 'POST',
                'Access-Control-Request-Headers': 'Content-Type'
            }
        });
        
        console.log('✅ CORS preflight successful:', response.status);
        console.log('Allowed methods:', response.headers.get('Access-Control-Allow-Methods'));
        console.log('Allowed headers:', response.headers.get('Access-Control-Allow-Headers'));
    } catch (error) {
        console.error('❌ CORS preflight failed:', error);
    }
};

// Run test
testCORS();
```

#### Backend CORS Configuration Reference

**Current Backend Configuration (CorsConfig.java):**
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;
    
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }
}
```

**Environment-Specific Configuration:**

**Development (application-dev.yml):**
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```

**Production (application-prod.yml):**
```yaml
cors:
  allowed-origins: https://app.islandhop.com,https://www.islandhop.com
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "Content-Type,Authorization,X-Requested-With"
  allow-credentials: true
```

### 🔍 CORS Debugging Checklist

- [ ] Backend server is running on `http://localhost:8084`
- [ ] Frontend origin is included in `cors.allowed-origins`
- [ ] Request method is included in `cors.allowed-methods`
- [ ] Request headers are allowed in `cors.allowed-headers`
- [ ] Browser Developer Tools Network tab shows successful OPTIONS preflight
- [ ] No browser extensions blocking CORS (disable ad blockers)
- [ ] Using correct protocol (http/https) consistently
- [ ] No trailing slashes in URLs causing redirect issues

**AI Integration Note:** The CORS configuration is already set up to work seamlessly with frontend applications. No additional CORS handling is required in your AI integration code.
