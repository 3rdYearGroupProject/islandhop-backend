# Frontend Integration Guide - Trip Planning System (Fully Updated with UserId Support)

## Table of Contents
1. [User Authentication & UserId Pattern](#user-authentication--userid-pattern)
2. [New Trip Planning Flow](#new-trip-planning-flow)
3. [Complete API Endpoint Reference](#complete-api-endpoint-reference)
4. [Request/Response Models](#request-response-models)
5. [Frontend Architecture](#frontend-architecture)
6. [UI Components Structure](#ui-components-structure)
7. [API Service Layer](#api-service-layer)
8. [State Management with Redux Toolkit](#state-management-with-redux-toolkit)
9. [User Interface Examples](#user-interface-examples)
10. [Category Management & Preference Mapping](#category-management--preference-mapping)
11. [Error Handling & Validation](#error-handling--validation)
12. [Testing Strategy](#testing-strategy)
13. [Security & User Authorization](#security--user-authorization)
14. [Microservice Architecture & User Identity](#microservice-architecture--user-identity)

## User Authentication & UserId Pattern

### 🔐 IMPORTANT: UserId is Required for All API Calls

**Critical Update:** All API endpoints now **require userId** to be included in request bodies (POST/PUT) or query parameters (GET). The old session-only pattern will **NOT work**.

#### Required Implementation Steps

1. **Get userId from your authentication system after login**
2. **Include userId in all API requests**
3. **Handle userId in responses for consistency**

#### Example: Getting UserId After Login
```javascript
// Step 1: Login and store userId
const loginUser = async (credentials) => {
  const response = await fetch('/api/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(credentials)
  });
  
  const userData = await response.json();
  
  // Store userId in your application state (Redux, Context, etc.)
  return {
    userId: userData.userId,     // ← CRITICAL: Store this for all future API calls
    email: userData.email,
    name: userData.name,
    isAuthenticated: true
  };
};

// Step 2: Use userId in all trip planning calls
const getCurrentUserId = () => {
  // Get from your auth state management
  // Examples:
  
  // Option A: From Redux store
  // const { user } = useSelector(state => state.auth);
  // return user?.userId;
  
  // Option B: From React Context
  // const { user } = useAuth();
  // return user?.userId;
  
  // Option C: From localStorage/sessionStorage
  // return sessionStorage.getItem('userId');
  
  // Option D: From JWT token
  // const token = localStorage.getItem('authToken');
  // const decoded = jwt_decode(token);
  // return decoded.userId;
  
  throw new Error('User not authenticated - implement getCurrentUserId()');
};

// Step 3: Use in trip planning calls
const createTrip = async (tripData) => {
  const userId = getCurrentUserId(); // Get current user's ID
  
  const response = await fetch('/api/trip/create-basic', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,              // ← REQUIRED in ALL requests
      tripName: tripData.name,
      startDate: tripData.startDate,
      endDate: tripData.endDate
    })
  });
  
  const result = await response.json();
  
  // Backend returns userId for consistency
  console.log('Trip created for user:', result.userId);
  return result;
};
```

#### ❌ Old Pattern (Will Fail)
```javascript
// DON'T DO THIS - Missing userId
const response = await fetch('/api/trip/create-basic', {
  method: 'POST',
  body: JSON.stringify({
    tripName: "My Trip",          // ❌ Missing userId
    startDate: "2024-12-01",
    endDate: "2024-12-07"
  })
});
```

#### ✅ New Pattern (Required)
```javascript
// DO THIS - Include userId
const response = await fetch('/api/trip/create-basic', {
  method: 'POST',
  body: JSON.stringify({
    userId: getCurrentUserId(),   // ✅ Required field
    tripName: "My Trip",
    startDate: "2024-12-01",
    endDate: "2024-12-07"
  })
});
```

## New Trip Planning Flow

### 🚀 Enhanced Multi-Step Trip Creation Workflow

The trip planning system now supports a modern, step-by-step approach designed for optimal user experience:

1. **Basic Trip Creation** - Name, start/end dates (minimal initial commitment)
2. **Preference Selection** - Terrain, activities, interests (personalization)
3. **City Selection** - Multi-city trips with day allocation (itinerary structure)
4. **Activity/Accommodation/Dining Search** - Preference-driven recommendations (content discovery)
5. **Day-by-Day Planning** - Detailed itinerary optimization (final planning)

### Complete Step-by-Step Implementation

#### Step 1: Basic Trip Creation
```javascript
// Create basic trip with minimal information to reduce initial friction
const createBasicTrip = async (tripData, userId) => {
  const response = await fetch('/api/trip/create-basic', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Essential for session management
    body: JSON.stringify({
      userId: userId,                 // ← REQUIRED: User identifier from auth
      tripName: tripData.name,        // User-friendly name
      startDate: tripData.startDate,  // "2024-12-01" (ISO date format)
      endDate: tripData.endDate       // "2024-12-07" (ISO date format)
    })
  });
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    tripId: result.tripId,
    trip: result.trip,
    message: result.message,
    userId: result.userId            // Backend returns userId for consistency
  };
};
```

#### Step 2: Update Preferences
```javascript
// Update user preferences for personalized recommendations
const updatePreferences = async (tripId, preferences, userId) => {
  const response = await fetch(`/api/trip/${tripId}/preferences`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,                        // ← REQUIRED: User identifier
      terrainPreferences: preferences.terrain,    // ["beach", "mountain", "urban", "rural"]
      activityPreferences: preferences.activities // ["adventure", "culture", "nightlife", "nature", "food", "shopping"]
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update preferences: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    userId: result.userId,
    trip: result.trip
  };
};
```

#### Step 3: Set Cities and Days
```javascript
// Configure multi-city trip with intelligent day allocation
const updateCities = async (tripId, cityData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/cities`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,                 // ← REQUIRED: User identifier
      cities: cityData.cities,        // ["Colombo", "Kandy", "Galle"]
      cityDays: cityData.cityDays     // {"Colombo": 2, "Kandy": 2, "Galle": 3}
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update cities: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    userId: result.userId,
    trip: result.trip
  };
};
```

#### Step 4: Search Activities/Accommodation/Dining
```javascript
// Search activities with preferences and proximity optimization
const searchActivities = async (tripId, searchParams) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',           // Optional text search
    city: searchParams.city || '',             // Filter by specific city
    lastPlaceId: searchParams.lastPlaceId || '', // For proximity recommendations
    maxResults: searchParams.maxResults || 10   // Pagination support
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/activities?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search activities: ${response.status}`);
  }
  
  return await response.json();
};

// Search accommodation with preference-based filtering
const searchAccommodation = async (tripId, searchParams) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',
    city: searchParams.city || '',
    lastPlaceId: searchParams.lastPlaceId || '',
    maxResults: searchParams.maxResults || 10
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/accommodation?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search accommodation: ${response.status}`);
  }
  
  return await response.json();
};

// Search dining options with preference and proximity matching
const searchDining = async (tripId, searchParams) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',
    city: searchParams.city || '',
    lastPlaceId: searchParams.lastPlaceId || '',
    maxResults: searchParams.maxResults || 10
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/dining?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search dining: ${response.status}`);
  }
  
  return await response.json();
};
```

## Complete API Endpoint Reference

### 🔗 Core Trip Management Endpoints

#### 1. Health Check
```javascript
// Service health verification - no authentication required
const healthCheck = async () => {
  const response = await fetch('/api/trip/health');
  
  if (!response.ok) {
    throw new Error(`Health check failed: ${response.status}`);
  }
  
  return await response.text(); // Returns: "Trip Planning Service is running"
};
```

#### 2. Create Basic Trip
```javascript
// Create initial trip with minimal information to reduce friction
const createBasicTrip = async (tripData, userId) => {
  const response = await fetch('/api/trip/create-basic', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Session required
    body: JSON.stringify({
      userId: userId,                 // ← REQUIRED: User identifier
      tripName: tripData.name,        // User-friendly trip name
      startDate: tripData.startDate,  // "2024-12-01" (ISO date format)
      endDate: tripData.endDate       // "2024-12-07" (ISO date format)
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to create basic trip: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    tripId: result.tripId,
    trip: result.trip,
    message: result.message,
    userId: result.userId            // Backend returns userId for consistency
  };
};
```

#### 3. Legacy Trip Creation (Full)
```javascript
// Create complete trip with all preferences (legacy approach - still supported)
const createFullTrip = async (tripData, userId) => {
  const response = await fetch('/api/trip/initiate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Session required
    body: JSON.stringify({
      userId: userId,                 // ← REQUIRED: User identifier
      tripName: tripData.name,        // "Cultural Explorer"
      startDate: tripData.startDate,  // "2024-12-01"
      endDate: tripData.endDate,      // "2024-12-07"
      baseCity: tripData.baseCity,    // "Colombo"
      categories: tripData.categories, // ["Culture", "Nature"]
      pacing: tripData.pacing         // "MODERATE"
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to create full trip: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    tripId: result.tripId,
    trip: result.trip,
    message: result.message,
    userId: result.userId
  };
};
```

#### 4. Update Trip Preferences
```javascript
// Update terrain and activity preferences for personalized recommendations
const updateTripPreferences = async (tripId, preferences, userId) => {
  const response = await fetch(`/api/trip/${tripId}/preferences`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Session required
    body: JSON.stringify({
      userId: userId,                        // ← REQUIRED: User identifier
      terrainPreferences: preferences.terrain,    // ["mountains", "beaches"]
      activityPreferences: preferences.activities // ["hiking", "swimming"]
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update preferences: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    userId: result.userId,
    trip: result.trip
  };
};
```

#### 5. Update Trip Cities
```javascript
// Set cities and day allocation for multi-city trips
const updateTripCities = async (tripId, cityData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/cities`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Session required
    body: JSON.stringify({
      userId: userId,                 // ← REQUIRED: User identifier
      cities: cityData.cities,        // ["Colombo", "Kandy", "Galle"]
      cityDays: cityData.cityDays     // {"Colombo": 2, "Kandy": 3, "Galle": 2}
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update cities: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    userId: result.userId,
    trip: result.trip
  };
};
```

### 🔍 Search Endpoints

#### 6. Search Activities
```javascript
// Find activities based on user preferences and proximity
const searchActivities = async (tripId, searchParams, userId = null) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',           // Optional text search
    city: searchParams.city || '',             // Filter by specific city
    lastPlaceId: searchParams.lastPlaceId || '', // For proximity recommendations
    maxResults: searchParams.maxResults || 10,   // Result limit
    ...(userId && { userId: userId })            // Optional userId for optimized validation
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/activities?${params}`, {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search activities: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    results: result.results,
    userId: result.userId
  };
};

// Example usage:
// const activities = await searchActivities('trip123', {
//   query: 'hiking',
//   city: 'Kandy',
//   maxResults: 20
// }, 'user123');
```

#### 7. Search Accommodation
```javascript
// Find hotels/lodging based on preferences
const searchAccommodation = async (tripId, searchParams, userId = null) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',           // Optional text search
    city: searchParams.city || '',             // Filter by specific city
    lastPlaceId: searchParams.lastPlaceId || '', // For proximity recommendations
    maxResults: searchParams.maxResults || 10,   // Result limit
    ...(userId && { userId: userId })            // Optional userId for optimized validation
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/accommodation?${params}`, {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search accommodation: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    results: result.results,
    userId: result.userId
  };
};

// Example usage:
// const hotels = await searchAccommodation('trip123', {
//   city: 'Galle'
// }, 'user123');
```

#### 8. Search Dining
```javascript
// Find restaurants based on preferences
const searchDining = async (tripId, searchParams, userId = null) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',           // Optional text search
    city: searchParams.city || '',             // Filter by specific city
    lastPlaceId: searchParams.lastPlaceId || '', // For proximity recommendations
    maxResults: searchParams.maxResults || 10,   // Result limit
    ...(userId && { userId: userId })            // Optional userId for optimized validation
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/dining?${params}`, {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search dining: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    results: result.results,
    userId: result.userId
  };
};

// Example usage:
// const restaurants = await searchDining('trip123', {
//   query: 'seafood',
//   city: 'Galle'
// }, 'user123');
```

### 📍 Location & Planning Endpoints

#### 9. Search Locations (General)
```javascript
// General location search with geographic bias
const searchLocations = async (searchParams) => {
  const params = new URLSearchParams({
    query: searchParams.query,                // Search text (required)
    city: searchParams.city || '',            // Optional city filter
    biasLat: searchParams.biasLat || '',      // Optional geographic bias
    biasLng: searchParams.biasLng || '',      // Optional geographic bias
    maxResults: searchParams.maxResults || 10 // Result limit
  });
  
  const response = await fetch(`/api/trip/search-locations?${params}`, {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search locations: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    results: result.results
  };
};

// Example usage:
// const locations = await searchLocations({
//   query: 'beaches',
//   city: 'Galle',
//   maxResults: 15
// });
```

#### 10. Add Place to Trip
```javascript
// Add a specific place to trip itinerary
const addPlaceToTrip = async (tripId, placeData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/add-place`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Session required
    body: JSON.stringify({
      userId: userId,                    // ← REQUIRED: User identifier
      placeName: placeData.name,         // "Temple of the Sacred Tooth Relic"
      city: placeData.city,              // "Kandy"
      description: placeData.description, // "Historical Buddhist temple"
      latitude: placeData.latitude,      // 7.2930
      longitude: placeData.longitude,    // 80.6346
      preferredDay: placeData.preferredDay || null // 2 (optional)
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to add place to trip: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    userId: result.userId,
    trip: result.trip
  };
};
```

#### 11. Add Place to Specific Day
```javascript
// Add place to a specific day with detailed context
const addPlaceToDay = async (tripId, dayNumber, placeData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/day/${dayNumber}/add-place`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Session required
    body: JSON.stringify({
      userId: userId,                             // ← REQUIRED: User identifier
      placeName: placeData.name,                  // "Sigiriya Rock Fortress"
      city: placeData.city,                       // "Dambulla"
      dayNumber: dayNumber,                       // 3
      placeType: placeData.type,                  // "ATTRACTION"
      estimatedVisitDurationMinutes: placeData.duration || 120, // 180
      preferredTimeSlot: placeData.timeSlot || 'morning',        // "morning"
      priority: placeData.priority || 5           // 8
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to add place to day: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    tripId: result.tripId,
    dayNumber: result.dayNumber,
    placeName: result.placeName,
    userId: result.userId,
    trip: result.trip
  };
};
```

#### 12. Generate/Optimize Itinerary
```javascript
// Create optimized day-by-day itinerary
const generateItinerary = async (tripId) => {
  const response = await fetch(`/api/trip/${tripId}/generate-itinerary`, {
    method: 'POST',
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to generate itinerary: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    message: result.message,
    userId: result.userId,
    trip: result.trip
  };
};
```

#### 13. Get Trip Details
```javascript
// Retrieve complete trip information
const getTripDetails = async (tripId) => {
  const response = await fetch(`/api/trip/${tripId}`, {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get trip details: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    trip: result.trip,
    userId: result.userId,
    tripId: result.tripId
  };
};
```

#### 14. Get User's Trips
```javascript
// List all trips for authenticated user
const getUserTrips = async () => {
  const response = await fetch('/api/trip/my-trips', {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get user trips: ${response.status}`);
  }
  
  const result = await response.json();
  return {
    trips: result.trips,
    userId: result.userId,
    count: result.count
  };
};
```

#### 15. Get Trip Map Data
```javascript
// Get data for map visualization
const getTripMapData = async (tripId) => {
  const response = await fetch(`/api/trip/${tripId}/map-data`, {
    credentials: 'include' // Session required
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get map data: ${response.status}`);
  }
  
  const result = await response.json();
  return result; // Map data object with coordinates and routes
};
```

## Request/Response Models

### 📝 Request DTOs (Updated with UserId Support)

#### CreateTripBasicRequest
```typescript
interface CreateTripBasicRequest {
  userId: string;       // User identifier (required)
  tripName: string;     // User-friendly trip name
  startDate: string;    // ISO date format "YYYY-MM-DD"
  endDate: string;      // ISO date format "YYYY-MM-DD"
}
```

#### UpdatePreferencesRequest
```typescript
interface UpdatePreferencesRequest {
  userId: string;                     // User identifier (required)
  terrainPreferences: string[];       // ["beach", "mountain", "urban", "rural"]
  activityPreferences: string[];      // ["adventure", "culture", "nightlife", "nature", "food", "shopping"]
}
```

#### UpdateCitiesRequest
```typescript
interface UpdateCitiesRequest {
  userId: string;                      // User identifier (required)
  cities: string[];                    // ["Colombo", "Kandy", "Galle"]
  cityDays: Record<string, number>;    // {"Colombo": 2, "Kandy": 2, "Galle": 3}
}
```

#### AddPlaceRequest
```typescript
interface AddPlaceRequest {
  userId: string;          // User identifier (required)
  placeName: string;       // Name of the place
  city?: string;           // City location
  description?: string;    // Optional description
  latitude?: number;       // GPS coordinates
  longitude?: number;      // GPS coordinates
  preferredDay?: number;   // Which day to visit (optional)
}
```

#### AddPlaceToDayRequest
```typescript
interface AddPlaceToDayRequest {
  userId: string;                           // User identifier (required)
  placeName: string;                        // Name of the place
  city?: string;                            // City location
  description?: string;                     // Optional description
  latitude?: number;                        // GPS coordinates
  longitude?: number;                       // GPS coordinates
  dayNumber: number;                        // Specific day number
  placeType: 'HOTEL' | 'ATTRACTION' | 'RESTAURANT' | 'ACTIVITY'; // Place category
  estimatedVisitDurationMinutes?: number;   // How long to spend here
  preferredTimeSlot?: 'morning' | 'afternoon' | 'evening'; // Time preference
  previousPlaceId?: string;                 // For contextual ordering
  isAccommodation?: boolean;                // Is this where they're staying
  priority?: number;                        // Importance level 1-10
}
```

#### CreateTripRequest (Legacy - Full Creation)
```typescript
interface CreateTripRequest {
  userId: string;          // User identifier (required)
  tripName: string;
  duration: number;                    // Days
  budget: number;                      // Budget in currency
  groupSize: number;                   // Number of people
  preferences: string[];               // Activity preferences
  terrainTypes: string[];              // Terrain preferences  
  startDate: string;                   // ISO date format
  endDate: string;                     // ISO date format
  cities?: string[];                   // Optional city list
}
```

### 📤 Response Models (Updated with UserId)

#### Standard Success Response
```typescript
interface ApiResponse<T> {
  message: string;      // Success message
  userId: string;       // User identifier (always included)
  tripId?: string;      // Trip identifier (when applicable)
  trip?: Trip;          // Trip object (when applicable)  
  results?: T[];        // Search results (for search endpoints)
  count?: number;       // Number of results (for list endpoints)
}
```

#### Search Response
```typescript
interface SearchResponse<T> {
  message: string;      // Search result message
  userId: string;       // User identifier
  results: T[];         // Array of search results
  tripId?: string;      // Trip context (when applicable)
}
```

#### Trip Model
```typescript
interface Trip {
  tripId: string;
  userId: string;
  tripName: string;
  duration: number;
  budget: number;
  groupSize: number;
  preferences: string[];
  terrainTypes: string[];
  startDate: string;
  endDate: string;
  
  // New fields for enhanced planning
  terrainPreferences: string[];
  activityPreferences: string[];
  plannedCities: string[];
  cityDays: Record<string, number>;
  
  places: PlannedPlace[];
  itinerary: DayPlan[];
  status: string;
  createdAt: string;
  updatedAt: string;
}
```

#### Location Search Result
```typescript
interface LocationSearchResult {
  locationId: string;
  name: string;
  description: string;
  category: string;
  subcategory: string;
  rating: number;
  reviewCount: number;
  coordinates: {
    latitude: number;
    longitude: number;
  };
  address: string;
  city: string;
  priceLevel: string;
  images: string[];
  preferenceScore: number;    // How well it matches user preferences
  proximityScore: number;     // How close it is to other planned locations
}
```

### ⚠️ Error Response Model
```typescript
interface ErrorResponse {
  error: string;          // Error type ("Unauthorized", "Bad Request", etc.)
  message: string;        // Detailed error message
  timestamp?: string;     // When the error occurred
  path?: string;          // API endpoint that failed
}
```

## Complete JavaScript API Integration Guide

### 🚀 All Endpoints with UserId Support - Ready-to-Use JavaScript Functions

#### Core Trip Management Functions

```javascript
// ===== TRIP CREATION =====

/**
 * Create a basic trip (NEW RECOMMENDED APPROACH)
 */
const createBasicTrip = async (tripData, userId) => {
  const response = await fetch('/api/trip/create-basic', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      tripName: tripData.name,
      startDate: tripData.startDate,
      endDate: tripData.endDate
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to create trip: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Create a full trip (LEGACY APPROACH - Still Supported)
 */
const createFullTrip = async (tripData, userId) => {
  const response = await fetch('/api/trip/initiate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      tripName: tripData.name,
      startDate: tripData.startDate,
      endDate: tripData.endDate,
      baseCity: tripData.baseCity,
      categories: tripData.categories,
      pacing: tripData.pacing,
      arrivalTime: tripData.arrivalTime,
      multiCity: tripData.multiCity || false
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to create full trip: ${response.status}`);
  }
  
  return await response.json();
};

// ===== TRIP PREFERENCES =====

/**
 * Update trip preferences
 */
const updateTripPreferences = async (tripId, preferences, userId) => {
  const response = await fetch(`/api/trip/${tripId}/preferences`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      terrainPreferences: preferences.terrain,
      activityPreferences: preferences.activity
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update preferences: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Update trip cities and day allocation
 */
const updateTripCities = async (tripId, cityData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/cities`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      cities: cityData.cities,
      cityDays: cityData.cityDays
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update cities: ${response.status}`);
  }
  
  return await response.json();
};

// ===== PLACE MANAGEMENT =====

/**
 * Add place to trip (general)
 */
const addPlaceToTrip = async (tripId, placeData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/add-place`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      placeName: placeData.name,
      city: placeData.city,
      description: placeData.description,
      latitude: placeData.latitude,
      longitude: placeData.longitude,
      preferredDay: placeData.preferredDay
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to add place: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Add place to specific day (enhanced)
 */
const addPlaceToDay = async (tripId, dayNumber, placeData, userId) => {
  const response = await fetch(`/api/trip/${tripId}/day/${dayNumber}/add-place`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      placeName: placeData.name,
      city: placeData.city,
      description: placeData.description,
      latitude: placeData.latitude,
      longitude: placeData.longitude,
      dayNumber: dayNumber,
      placeType: placeData.type, // "HOTEL", "ATTRACTION", "RESTAURANT", "ACTIVITY"
      estimatedVisitDurationMinutes: placeData.duration,
      preferredTimeSlot: placeData.timeSlot, // "morning", "afternoon", "evening"
      previousPlaceId: placeData.previousPlaceId,
      isAccommodation: placeData.isAccommodation || false,
      priority: placeData.priority || 5
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to add place to day: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Quick add place (inline without navigation)
 */
const quickAddPlace = async (tripId, dayNumber, placeData) => {
  const params = new URLSearchParams({
    placeId: placeData.id,
    placeName: placeData.name,
    placeType: placeData.type,
    insertAfterPlaceId: placeData.insertAfter || ''
  });
  
  const response = await fetch(`/api/trip/${tripId}/day/${dayNumber}/quick-add?${params}`, {
    method: 'POST',
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to quick add place: ${response.status}`);
  }
  
  return await response.json();
};

// ===== SEARCH FUNCTIONS =====

/**
 * Search activities with user preferences
 */
const searchActivities = async (tripId, searchParams, userId = null) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',
    city: searchParams.city || '',
    lastPlaceId: searchParams.lastPlaceId || '',
    maxResults: searchParams.maxResults || 10,
    ...(userId && { userId: userId })
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/activities?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search activities: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Search accommodation with preferences
 */
const searchAccommodation = async (tripId, searchParams, userId = null) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',
    city: searchParams.city || '',
    lastPlaceId: searchParams.lastPlaceId || '',
    maxResults: searchParams.maxResults || 10,
    ...(userId && { userId: userId })
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/accommodation?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search accommodation: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Search dining options with preferences
 */
const searchDining = async (tripId, searchParams, userId = null) => {
  const params = new URLSearchParams({
    query: searchParams.query || '',
    city: searchParams.city || '',
    lastPlaceId: searchParams.lastPlaceId || '',
    maxResults: searchParams.maxResults || 10,
    ...(userId && { userId: userId })
  });
  
  const response = await fetch(`/api/trip/${tripId}/search/dining?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search dining: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * General location search
 */
const searchLocations = async (searchParams) => {
  const params = new URLSearchParams({
    query: searchParams.query,
    city: searchParams.city || '',
    biasLat: searchParams.biasLat || '',
    biasLng: searchParams.biasLng || '',
    maxResults: searchParams.maxResults || 10
  });
  
  const response = await fetch(`/api/trip/search-locations?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search locations: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Contextual location search (trip-aware)
 */
const contextualLocationSearch = async (tripId, searchParams) => {
  const params = new URLSearchParams({
    query: searchParams.query,
    placeType: searchParams.placeType || '',
    dayNumber: searchParams.dayNumber || '',
    lastPlaceId: searchParams.lastPlaceId || '',
    maxResults: searchParams.maxResults || 10
  });
  
  const response = await fetch(`/api/trip/${tripId}/contextual-search?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to search contextually: ${response.status}`);
  }
  
  return await response.json();
};

// ===== TRIP INFORMATION =====

/**
 * Get user's trips
 */
const getUserTrips = async () => {
  const response = await fetch('/api/trip/my-trips', {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get trips: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get trip summary
 */
const getTripSummary = async (tripId) => {
  const response = await fetch(`/api/trip/${tripId}/summary`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get trip summary: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get day plan
 */
const getDayPlan = async (tripId, day) => {
  const response = await fetch(`/api/trip/${tripId}/day/${day}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get day plan: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get day plan with suggestions (TripAdvisor style)
 */
const getDayPlanWithSuggestions = async (tripId, dayNumber) => {
  const response = await fetch(`/api/trip/${tripId}/day/${dayNumber}/plan`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get day plan with suggestions: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get map data for trip
 */
const getMapData = async (tripId) => {
  const response = await fetch(`/api/trip/${tripId}/map-data`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get map data: ${response.status}`);
  }
  
  return await response.json();
};

// ===== SUGGESTIONS =====

/**
 * Get AI-powered suggestions
 */
const getSuggestions = async (tripId, day = null) => {
  const params = new URLSearchParams({
    ...(day && { day: day })
  });
  
  const response = await fetch(`/api/trip/${tripId}/suggestions?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get suggestions: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get contextual suggestions for a day
 */
const getContextualSuggestions = async (tripId, dayNumber, contextType = 'initial') => {
  const params = new URLSearchParams({
    contextType: contextType
  });
  
  const response = await fetch(`/api/trip/${tripId}/day/${dayNumber}/contextual-suggestions?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get contextual suggestions: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get real-time suggestions
 */
const getRealtimeSuggestions = async (tripId, dayNumber, options = {}) => {
  const params = new URLSearchParams({
    lastPlaceId: options.lastPlaceId || '',
    category: options.category || ''
  });
  
  const response = await fetch(`/api/trip/${tripId}/day/${dayNumber}/realtime-suggestions?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get realtime suggestions: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get nearby suggestions
 */
const getNearbySuggestions = async (tripId, placeId, options = {}) => {
  const params = new URLSearchParams({
    placeId: placeId,
    placeType: options.placeType || '',
    maxResults: options.maxResults || 10
  });
  
  const response = await fetch(`/api/trip/${tripId}/nearby-suggestions?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get nearby suggestions: ${response.status}`);
  }
  
  return await response.json();
};

// ===== OPTIMIZATION =====

/**
 * Optimize trip order
 */
const optimizeOrder = async (tripId) => {
  const response = await fetch(`/api/trip/${tripId}/optimize-order`, {
    method: 'POST',
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to optimize order: ${response.status}`);
  }
  
  return await response.json();
};

// ===== PLACE INFORMATION =====

/**
 * Get place details
 */
const getPlaceDetails = async (placeId) => {
  const response = await fetch(`/api/trip/place-details/${placeId}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get place details: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Validate place information
 */
const validatePlace = async (placeData, userId) => {
  const response = await fetch('/api/trip/validate-place', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      userId: userId,
      placeName: placeData.name,
      city: placeData.city,
      description: placeData.description,
      latitude: placeData.latitude,
      longitude: placeData.longitude
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to validate place: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get place categories
 */
const getPlaceCategories = async () => {
  const response = await fetch('/api/trip/place-categories', {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get place categories: ${response.status}`);
  }
  
  return await response.json();
};

// ===== TRAVEL INFORMATION =====

/**
 * Get travel information between places
 */
const getTravelInfo = async (tripId, fromPlaceId, toPlaceId) => {
  const params = new URLSearchParams({
    fromPlaceId: fromPlaceId,
    toPlaceId: toPlaceId
  });
  
  const response = await fetch(`/api/trip/${tripId}/travel-info?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get travel info: ${response.status}`);
  }
  
  return await response.json();
};

/**
 * Get enhanced travel information
 */
const getEnhancedTravelInfo = async (tripId, fromPlaceId, toPlaceId) => {
  const params = new URLSearchParams({
    fromPlaceId: fromPlaceId,
    toPlaceId: toPlaceId
  });
  
  const response = await fetch(`/api/trip/${tripId}/enhanced-travel-info?${params}`, {
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Failed to get enhanced travel info: ${response.status}`);
  }
  
  return await response.json();
};

// ===== UTILITY FUNCTIONS =====

/**
 * Health check
 */
const healthCheck = async () => {
  const response = await fetch('/api/trip/health');
  return await response.text();
};

/**
 * Complete API service class for easy integration
 */
class TripPlanningAPI {
  constructor(baseUrl = '/api/trip') {
    this.baseUrl = baseUrl;
  }
  
  // Helper method for making requests
  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const defaultOptions = {
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      }
    };
    
    const response = await fetch(url, { ...defaultOptions, ...options });
    
    if (!response.ok) {
      throw new Error(`API request failed: ${response.status} ${response.statusText}`);
    }
    
    return await response.json();
  }
  
  // Trip management methods
  async createBasicTrip(tripData, userId) {
    return this.request('/create-basic', {
      method: 'POST',
      body: JSON.stringify({ userId, ...tripData })
    });
  }
  
  async updatePreferences(tripId, preferences, userId) {
    return this.request(`/${tripId}/preferences`, {
      method: 'POST',
      body: JSON.stringify({ userId, ...preferences })
    });
  }
  
  async updateCities(tripId, cityData, userId) {
    return this.request(`/${tripId}/cities`, {
      method: 'POST',
      body: JSON.stringify({ userId, ...cityData })
    });
  }
  
  async searchActivities(tripId, searchParams, userId = null) {
    const params = new URLSearchParams({
      ...searchParams,
      ...(userId && { userId })
    });
    return this.request(`/${tripId}/search/activities?${params}`);
  }
  
  async searchAccommodation(tripId, searchParams, userId = null) {
    const params = new URLSearchParams({
      ...searchParams,
      ...(userId && { userId })
    });
    return this.request(`/${tripId}/search/accommodation?${params}`);
  }
  
  async searchDining(tripId, searchParams, userId = null) {
    const params = new URLSearchParams({
      ...searchParams,
      ...(userId && { userId })
    });
    return this.request(`/${tripId}/search/dining?${params}`);
  }
  
  // Add more methods as needed...
}

// Export for ES6 modules
export {
  createBasicTrip,
  createFullTrip,
  updateTripPreferences,
  updateTripCities,
  addPlaceToTrip,
  addPlaceToDay,
  quickAddPlace,
  searchActivities,
  searchAccommodation,
  searchDining,
  searchLocations,
  contextualLocationSearch,
  getUserTrips,
  getTripSummary,
  getDayPlan,
  getDayPlanWithSuggestions,
  getMapData,
  getSuggestions,
  getContextualSuggestions,
  getRealtimeSuggestions,
  getNearbySuggestions,
  optimizeOrder,
  getPlaceDetails,
  validatePlace,
  getPlaceCategories,
  getTravelInfo,
  getEnhancedTravelInfo,
  healthCheck,
  TripPlanningAPI
};
```

### 🔧 React Integration Examples

#### Complete Trip Creation Flow
```jsx
import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { createBasicTrip, updateTripPreferences, updateTripCities } from './tripAPI';

const TripCreationWizard = () => {
  const currentUser = useSelector(state => state.auth.user);
  const [step, setStep] = useState(1);
  const [tripData, setTripData] = useState({});
  const [tripId, setTripId] = useState(null);
  
  const handleStepOne = async (formData) => {
    try {
      const result = await createBasicTrip({
        tripName: formData.name,
        startDate: formData.startDate,
        endDate: formData.endDate
      }, currentUser.userId);  // ← Pass userId as second parameter
      
      setTripId(result.tripId);
      setTripData(prev => ({ ...prev, ...result.trip }));
      setStep(2);
    } catch (error) {
      console.error('Failed to create trip:', error);
    }
  };
  
  const handleStepTwo = async (preferences) => {
    try {
      await updateTripPreferences(tripId, preferences, currentUser.userId); // ← Pass userId
      setStep(3);
    } catch (error) {
      console.error('Failed to update preferences:', error);
    }
  };
  
  const handleStepThree = async (cityData) => {
    try {
      await updateTripCities(tripId, cityData, currentUser.userId); // ← Pass userId
      // Navigate to planning interface
      window.location.href = `/trip/${tripId}/planning`;
    } catch (error) {
      console.error('Failed to update cities:', error);
    }
  };
  
  return (
    <div>
      {step === 1 && <BasicTripForm onSubmit={handleStepOne} />}
      {step === 2 && <PreferencesForm onSubmit={handleStepTwo} />}
      {step === 3 && <CitiesForm onSubmit={handleStepThree} />}
    </div>
  );
};
```

#### Activity Search Component
```jsx
import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { searchActivities, addPlaceToDay } from './tripAPI';

const ActivitySearch = ({ tripId, currentDay }) => {
  const currentUser = useSelector(state => state.auth.user);
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  
  const handleSearch = async () => {
    setLoading(true);
    try {
      const searchResults = await searchActivities(tripId, {
        query: searchQuery,
        maxResults: 20
      }, currentUser.userId);
      
      setResults(searchResults.results);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const handleAddToTrip = async (activity) => {
    try {
      await addPlaceToDay(tripId, currentDay, {
        name: activity.name,
        city: activity.city,
        description: activity.description,
        latitude: activity.latitude,
        longitude: activity.longitude,
        type: 'ATTRACTION',
        duration: activity.suggestedDuration || 120,
        timeSlot: 'afternoon',
        priority: 7
      }, currentUser.userId);
      
      alert('Activity added to trip!');
    } catch (error) {
      console.error('Failed to add activity:', error);
    }
  };
  
  return (
    <div>
      <input 
        type="text" 
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        placeholder="Search for activities..."
      />
      <button onClick={handleSearch} disabled={loading}>
        {loading ? 'Searching...' : 'Search'}
      </button>
      
      <div>
        {results.map(activity => (
          <div key={activity.id} className="activity-card">
            <h3>{activity.name}</h3>
            <p>{activity.description}</p>
            <p>Location: {activity.city}</p>
            <p>Rating: {activity.rating}/5</p>
            <button onClick={() => handleAddToTrip(activity)}>
              Add to Day {currentDay}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};
```
