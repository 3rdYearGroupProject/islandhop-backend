# Frontend Integration Guide - Trip Planning System (Fully Updated)

## Table of Contents
1. [New Trip Planning Flow](#new-trip-planning-flow)
2. [Complete API Endpoint Reference](#complete-api-endpoint-reference)
3. [Request/Response Models](#request-response-models)
4. [Frontend Architecture](#frontend-architecture)
5. [UI Components Structure](#ui-components-structure)
6. [API Service Layer](#api-service-layer)
7. [State Management with Redux Toolkit](#state-management-with-redux-toolkit)
8. [User Interface Examples](#user-interface-examples)
9. [Category Management & Preference Mapping](#category-management--preference-mapping)
10. [Error Handling & Validation](#error-handling--validation)
11. [Testing Strategy](#testing-strategy)

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
const createBasicTrip = async (tripData) => {
  const response = await fetch('/api/trip/create-basic', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Essential for session management
    body: JSON.stringify({
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
    message: result.message
  };
};
```

#### Step 2: Update Preferences
```javascript
// Update user preferences for personalized recommendations
const updatePreferences = async (tripId, preferences) => {
  const response = await fetch(`/api/trip/${tripId}/preferences`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      terrainPreferences: preferences.terrain,    // ["beach", "mountain", "urban", "rural"]
      activityPreferences: preferences.activities // ["adventure", "culture", "nightlife", "nature", "food", "shopping"]
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update preferences: ${response.status}`);
  }
  
  return await response.json();
};
```

#### Step 3: Set Cities and Days
```javascript
// Configure multi-city trip with intelligent day allocation
const updateCities = async (tripId, cityData) => {
  const response = await fetch(`/api/trip/${tripId}/cities`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      cities: cityData.cities,        // ["Colombo", "Kandy", "Galle"]
      cityDays: cityData.cityDays     // {"Colombo": 2, "Kandy": 2, "Galle": 3}
    })
  });
  
  if (!response.ok) {
    throw new Error(`Failed to update cities: ${response.status}`);
  }
  
  return await response.json();
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
```
GET /api/trip/health
Description: Service health verification
Authentication: None required
Response: String "Trip Planning Service is running"
```

#### 2. Create Basic Trip
```
POST /api/trip/create-basic
Description: Create initial trip with minimal information
Authentication: Session required
Request Body: CreateTripBasicRequest
Response: { message, tripId, trip }
```

#### 3. Legacy Trip Creation (Full)
```
POST /api/trip/initiate
Description: Create complete trip with all preferences
Authentication: Session required  
Request Body: CreateTripRequest
Response: { message, tripId, trip }
```

#### 4. Update Trip Preferences
```
POST /api/trip/{tripId}/preferences
Description: Update terrain and activity preferences
Authentication: Session required
Request Body: UpdatePreferencesRequest
Response: { message, trip }
```

#### 5. Update Trip Cities
```
POST /api/trip/{tripId}/cities
Description: Set cities and day allocation
Authentication: Session required
Request Body: UpdateCitiesRequest
Response: { message, trip }
```

### 🔍 Search Endpoints

#### 6. Search Activities
```
GET /api/trip/{tripId}/search/activities
Description: Find activities based on user preferences and proximity
Authentication: Session required
Query Parameters:
  - query (optional): Text search
  - city (optional): Filter by city
  - lastPlaceId (optional): For proximity recommendations
  - maxResults (optional, default=10): Result limit
Response: { message, results }
```

#### 7. Search Accommodation
```
GET /api/trip/{tripId}/search/accommodation
Description: Find hotels/lodging based on preferences
Authentication: Session required
Query Parameters: Same as activities
Response: { message, results }
```

#### 8. Search Dining
```
GET /api/trip/{tripId}/search/dining
Description: Find restaurants based on preferences
Authentication: Session required
Query Parameters: Same as activities
Response: { message, results }
```

### 📍 Location & Planning Endpoints

#### 9. Search Locations (General)
```
GET /api/trip/search-locations
Description: General location search with bias
Authentication: Session required
Query Parameters:
  - query: Search text (required)
  - city (optional): City filter
  - biasLat, biasLng (optional): Geographic bias
  - maxResults (optional, default=10): Result limit
Response: { results }
```

#### 10. Add Place to Trip
```
POST /api/trip/{tripId}/add-place
Description: Add a specific place to trip itinerary
Authentication: Session required
Request Body: AddPlaceRequest
Response: { message, trip }
```

#### 11. Generate/Optimize Itinerary
```
POST /api/trip/{tripId}/generate-itinerary
Description: Create optimized day-by-day itinerary
Authentication: Session required
Response: { message, trip }
```

#### 12. Get Trip Details
```
GET /api/trip/{tripId}
Description: Retrieve complete trip information
Authentication: Session required
Response: { trip }
```

#### 13. Get User's Trips
```
GET /api/trip/my-trips
Description: List all trips for authenticated user
Authentication: Session required
Response: { trips }
```

#### 14. Get Trip Map Data
```
GET /api/trip/{tripId}/map-data
Description: Get data for map visualization
Authentication: Session required
Response: Map data object with coordinates and routes
```

## Request/Response Models

### 📝 Request DTOs

#### CreateTripBasicRequest
```typescript
interface CreateTripBasicRequest {
  tripName: string;     // User-friendly trip name
  startDate: string;    // ISO date format "YYYY-MM-DD"
  endDate: string;      // ISO date format "YYYY-MM-DD"
}
```

#### UpdatePreferencesRequest
```typescript
interface UpdatePreferencesRequest {
  terrainPreferences: string[];   // ["beach", "mountain", "urban", "rural"]
  activityPreferences: string[];  // ["adventure", "culture", "nightlife", "nature", "food", "shopping"]
}
```

#### UpdateCitiesRequest
```typescript
interface UpdateCitiesRequest {
  cities: string[];                    // ["Colombo", "Kandy", "Galle"]
  cityDays: Record<string, number>;    // {"Colombo": 2, "Kandy": 2, "Galle": 3}
}
```

#### CreateTripRequest (Legacy - Full Creation)
```typescript
interface CreateTripRequest {
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

### 📤 Response Models

#### Standard Success Response
```typescript
interface ApiResponse<T> {
  message: string;      // Success message
  tripId?: string;      // Trip identifier (when applicable)
  trip?: Trip;          // Trip object (when applicable)  
  results?: T[];        // Search results (for search endpoints)
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

// Similar functions for accommodation and dining
const searchAccommodation = async (tripId, searchParams) => { /* ... */ };
const searchDining = async (tripId, searchParams) => { /* ... */ };
```

## API Endpoint Integration

### Base Configuration
```javascript
// config/api.js
const API_BASE_URL = 'http://localhost:8083'; // Updated port
const TRIP_API_BASE = `${API_BASE_URL}/api/trip`;

export const API_ENDPOINTS = {
  // === NEW TRIP FLOW ENDPOINTS ===
  
  // Basic Trip Management
  CREATE_BASIC_TRIP: `${TRIP_API_BASE}/create-basic`,
  UPDATE_PREFERENCES: (tripId) => `${TRIP_API_BASE}/${tripId}/preferences`,
  UPDATE_CITIES: (tripId) => `${TRIP_API_BASE}/${tripId}/cities`,
  
  // Search Endpoints (NEW)
  SEARCH_ACTIVITIES: (tripId) => `${TRIP_API_BASE}/${tripId}/search/activities`,
  SEARCH_ACCOMMODATION: (tripId) => `${TRIP_API_BASE}/${tripId}/search/accommodation`,
  SEARCH_DINING: (tripId) => `${TRIP_API_BASE}/${tripId}/search/dining`,
  
  // === EXISTING ENDPOINTS ===
  
  // Legacy Trip Management
  CREATE_TRIP: `${TRIP_API_BASE}/initiate`,
  GET_MY_TRIPS: `${TRIP_API_BASE}/my-trips`,
  GET_TRIP_SUMMARY: (tripId) => `${TRIP_API_BASE}/${tripId}/summary`,
  
  // Location & Search
  SEARCH_LOCATIONS: `${TRIP_API_BASE}/search-locations`,
  ADD_PLACE: (tripId) => `${TRIP_API_BASE}/${tripId}/add-place`,
  ADD_PLACE_TO_DAY: (tripId, day) => `${TRIP_API_BASE}/${tripId}/day/${day}/add-place`,
  
  // AI Features
  GET_SUGGESTIONS: (tripId) => `${TRIP_API_BASE}/${tripId}/suggestions`,
  GET_CONTEXTUAL_SUGGESTIONS: (tripId, day) => `${TRIP_API_BASE}/${tripId}/day/${day}/contextual-suggestions`,
  GET_NEARBY_SUGGESTIONS: (tripId) => `${TRIP_API_BASE}/${tripId}/nearby-suggestions`,
  
  // Day Planning
  GET_DAY_PLAN: (tripId, day) => `${TRIP_API_BASE}/${tripId}/day/${day}`,
  OPTIMIZE_ORDER: (tripId) => `${TRIP_API_BASE}/${tripId}/optimize-order`,
  
  // Map & Data
  GET_MAP_DATA: (tripId) => `${TRIP_API_BASE}/${tripId}/map-data`,
  
  // Utility
  HEALTH_CHECK: `${TRIP_API_BASE}/health`
};
```

### Request/Response Data Models

#### CreateTripBasicRequest
```typescript
interface CreateTripBasicRequest {
  tripName: string;      // "Sri Lanka Adventure"
  startDate: string;     // "2024-12-01" (ISO date format)
  endDate: string;       // "2024-12-07" (ISO date format)
}
```

#### UpdatePreferencesRequest
```typescript
interface UpdatePreferencesRequest {
  terrainPreferences: string[];    // ["beach", "mountain", "urban", "forest"]
  activityPreferences: string[];   // ["adventure", "culture", "nightlife", "nature", "wellness", "shopping"]
}

// Available preference values:
const TERRAIN_OPTIONS = [
  "beach", "mountain", "urban", "forest", "countryside", "lakeside", "desert", "volcanic"
];

const ACTIVITY_OPTIONS = [
  "adventure", "culture", "nightlife", "nature", "wellness", "shopping", 
  "sports", "photography", "food", "history", "wildlife", "relaxation"
];
```

#### UpdateCitiesRequest
```typescript
interface UpdateCitiesRequest {
  cities: string[];                    // ["Colombo", "Kandy", "Galle"]
  cityDays: Record<string, number>;    // {"Colombo": 2, "Kandy": 2, "Galle": 3}
}
```

#### Search Response Model
```typescript
interface SearchResult {
  placeId: string;
  name: string;
  formattedAddress: string;
  latitude: number;
  longitude: number;
  rating?: number;
  types: string[];
  source: string;           // "tripadvisor", "google", "local"
  distanceFromBias?: number; // Distance from last selected place (km)
}

interface SearchResponse {
  message: string;
  results: SearchResult[];
}
```
  "framework": "React.js / Vue.js / Next.js",
  "stateManagement": "Redux Toolkit / Zustand / Pinia",
  "styling": "Tailwind CSS / Material-UI / Ant Design",
  "maps": "Google Maps API / Leaflet",
  "http": "Axios / Fetch API",
  "forms": "Formik / React Hook Form",
  "datePicker": "react-datepicker / vuetify-datepicker",
  "notifications": "react-toastify / vue-toastification"
}
```

### Project Structure
```
src/
├── components/
│   ├── TripCreation/
│   │   ├── TripWizard.jsx
│   │   ├── CitySelector.jsx
│   │   ├── CategorySelector.jsx
│   │   └── DateRangePicker.jsx
│   ├── TripPlanning/
│   │   ├── TripDashboard.jsx
│   │   ├── LocationSearch.jsx
│   │   ├── PlaceCard.jsx
│   │   └── SuggestionPanel.jsx
│   ├── DayPlanning/
│   │   ├── DayPlanView.jsx
│   │   ├── TimelineView.jsx
│   │   └── PlaceTimeline.jsx
│   ├── Maps/
│   │   ├── TripMap.jsx
│   │   └── RouteVisualization.jsx
│   └── Common/
│       ├── LoadingSpinner.jsx
│       ├── ErrorBoundary.jsx
│       └── CategoryChip.jsx
├── services/
│   ├── tripService.js
│   ├── locationService.js
│   └── apiClient.js
├── store/
│   ├── tripSlice.js
│   ├── locationSlice.js
│   └── store.js
├── pages/
│   ├── TripCreation.jsx
│   ├── TripPlanning.jsx
│   ├── DayPlanning.jsx
│   └── TripSummary.jsx
└── utils/
    ├── dateHelpers.js
    ├── categoryHelpers.js
    └── mapHelpers.js
```

## API Service Layer

### Trip Service Implementation
```javascript
// services/tripService.js
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

class TripService {
  constructor() {
    this.api = axios.create({
      baseURL: API_ENDPOINTS.base,
      withCredentials: true, // Important for session handling
      timeout: 30000
    });
    
    // Request interceptor for loading states
    this.api.interceptors.request.use(
      (config) => {
        // Show loading spinner
        return config;
      },
      (error) => Promise.reject(error)
    );
    
    // Response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response.data,
      (error) => {
        if (error.response?.status === 401) {
          // Redirect to login
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Trip Creation
  async createTrip(tripData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.CREATE_TRIP, {
        tripName: tripData.tripName,
        startDate: tripData.startDate,
        endDate: tripData.endDate,
        arrivalTime: tripData.arrivalTime,
        baseCity: tripData.baseCity,
        multiCity: tripData.multiCity,
        categories: tripData.categories,
        pacing: tripData.pacing
      });
      return response;
    } catch (error) {
      throw new Error(`Failed to create trip: ${error.message}`);
    }
  }

  // Location Search
  async searchLocations(query, city = null, biasLocation = null, maxResults = 10) {
    try {
      const params = {
        query,
        maxResults,
        ...(city && { city }),
        ...(biasLocation && { 
          biasLat: biasLocation.lat, 
          biasLng: biasLocation.lng 
        })
      };
      
      const response = await this.api.get(API_ENDPOINTS.SEARCH_LOCATIONS, { params });
      return response.results;
    } catch (error) {
      throw new Error(`Failed to search locations: ${error.message}`);
    }
  }

  // Add Place to Trip
  async addPlaceToTrip(tripId, placeData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.ADD_PLACE(tripId), {
        placeName: placeData.name,
        address: placeData.address,
        latitude: placeData.latitude,
        longitude: placeData.longitude,
        category: placeData.category,
        placeId: placeData.placeId,
        estimatedDuration: placeData.estimatedDuration || "2 hours"
      });
      return response;
    } catch (error) {
      throw new Error(`Failed to add place: ${error.message}`);
    }
  }

  // Get AI Suggestions
  async getSuggestions(tripId, day = null) {
    try {
      const params = day ? { day } : {};
      const response = await this.api.get(API_ENDPOINTS.GET_SUGGESTIONS(tripId), { params });
      return response;
    } catch (error) {
      throw new Error(`Failed to get suggestions: ${error.message}`);
    }
  }

  // Get Day Plan
  async getDayPlan(tripId, day) {
    try {
      const response = await this.api.get(API_ENDPOINTS.GET_DAY_PLAN(tripId, day));
      return response;
    } catch (error) {
      throw new Error(`Failed to get day plan: ${error.message}`);
    }
  }

  // Optimize Trip Order
  async optimizeTripOrder(tripId) {
    try {
      const response = await this.api.post(API_ENDPOINTS.OPTIMIZE_ORDER(tripId));
      return response;
    } catch (error) {
      throw new Error(`Failed to optimize trip: ${error.message}`);
    }
  }

  // Get Trip Summary
  async getTripSummary(tripId) {
    try {
      const response = await this.api.get(API_ENDPOINTS.GET_TRIP_SUMMARY(tripId));
      return response;
    } catch (error) {
      throw new Error(`Failed to get trip summary: ${error.message}`);
    }
  }

  // Get User's Trips
  async getMyTrips() {
    try {
      const response = await this.api.get(API_ENDPOINTS.GET_MY_TRIPS);
      return response.trips;
    } catch (error) {
      throw new Error(`Failed to get trips: ${error.message}`);
    }
  }
}

export default new TripService();
```

## Frontend Architecture

### Technology Stack Recommendation
```json
{
  "framework": "React 18+ with TypeScript",
  "stateManagement": "Redux Toolkit + RTK Query",
  "routing": "React Router v6",
  "styling": "Tailwind CSS + Headless UI",
  "forms": "React Hook Form + Zod validation",
  "dateHandling": "date-fns",
  "mapping": "Leaflet or Google Maps API",
  "icons": "Heroicons or Lucide React"
}
```

### Modern Trip Planning Service Integration

```typescript
// services/tripPlanningService.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const tripPlanningApi = createApi({
  reducerPath: 'tripPlanningApi',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api/trip',
    credentials: 'include',
    prepareHeaders: (headers) => {
      headers.set('Content-Type', 'application/json');
      return headers;
    },
  }),
  tagTypes: ['Trip', 'SearchResults'],
  endpoints: (builder) => ({
    // === NEW TRIP FLOW ===
    createBasicTrip: builder.mutation<TripResponse, CreateTripBasicRequest>({
      query: (tripData) => ({
        url: '/create-basic',
        method: 'POST',
        body: tripData,
      }),
      invalidatesTags: ['Trip'],
    }),
    
    updateTripPreferences: builder.mutation<TripResponse, { tripId: string; preferences: UpdatePreferencesRequest }>({
      query: ({ tripId, preferences }) => ({
        url: `/${tripId}/preferences`,
        method: 'POST',
        body: preferences,
      }),
      invalidatesTags: (result, error, { tripId }) => [{ type: 'Trip', id: tripId }],
    }),
    
    updateTripCities: builder.mutation<TripResponse, { tripId: string; cities: UpdateCitiesRequest }>({
      query: ({ tripId, cities }) => ({
        url: `/${tripId}/cities`,
        method: 'POST',
        body: cities,
      }),
      invalidatesTags: (result, error, { tripId }) => [{ type: 'Trip', id: tripId }],
    }),
    
    // === SEARCH ENDPOINTS ===
    searchActivities: builder.query<SearchResponse, SearchActivitiesParams>({
      query: ({ tripId, ...params }) => ({
        url: `/${tripId}/search/activities`,
        params: params,
      }),
      providesTags: (result, error, { tripId }) => [{ type: 'SearchResults', id: `activities-${tripId}` }],
    }),
    
    searchAccommodation: builder.query<SearchResponse, SearchAccommodationParams>({
      query: ({ tripId, ...params }) => ({
        url: `/${tripId}/search/accommodation`,
        params: params,
      }),
      providesTags: (result, error, { tripId }) => [{ type: 'SearchResults', id: `accommodation-${tripId}` }],
    }),
    
    searchDining: builder.query<SearchResponse, SearchDiningParams>({
      query: ({ tripId, ...params }) => ({
        url: `/${tripId}/search/dining`,
        params: params,
      }),
      providesTags: (result, error, { tripId }) => [{ type: 'SearchResults', id: `dining-${tripId}` }],
    }),
    
    // === EXISTING ENDPOINTS ===
    getMyTrips: builder.query<Trip[], void>({
      query: () => '/my-trips',
      providesTags: ['Trip'],
    }),
    
    getTripSummary: builder.query<Trip, string>({
      query: (tripId) => `/${tripId}/summary`,
      providesTags: (result, error, tripId) => [{ type: 'Trip', id: tripId }],
    }),
    
    // ... other existing endpoints
  }),
});

export const {
  useCreateBasicTripMutation,
  useUpdateTripPreferencesMutation,
  useUpdateTripCitiesMutation,
  useSearchActivitiesQuery,
  useSearchAccommodationQuery,
  useSearchDiningQuery,
  useGetMyTripsQuery,
  useGetTripSummaryQuery,
} = tripPlanningApi;
```

## UI Components Structure

### 1. Trip Creation Wizard

```tsx
// components/TripCreation/TripCreationWizard.tsx
import React, { useState } from 'react';
import { useCreateBasicTripMutation, useUpdateTripPreferencesMutation, useUpdateTripCitiesMutation } from '../services/tripPlanningService';

interface TripCreationWizardProps {
  onComplete: (tripId: string) => void;
}

const TripCreationWizard: React.FC<TripCreationWizardProps> = ({ onComplete }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [tripData, setTripData] = useState({
    tripName: '',
    startDate: '',
    endDate: '',
    preferences: {
      terrainPreferences: [] as string[],
      activityPreferences: [] as string[],
    },
    cities: {
      cities: [] as string[],
      cityDays: {} as Record<string, number>,
    },
  });
  
  const [createBasicTrip] = useCreateBasicTripMutation();
  const [updatePreferences] = useUpdateTripPreferencesMutation();
  const [updateCities] = useUpdateTripCitiesMutation();
  
  const [tripId, setTripId] = useState<string | null>(null);

  const handleStep1Submit = async () => {
    try {
      const result = await createBasicTrip({
        tripName: tripData.tripName,
        startDate: tripData.startDate,
        endDate: tripData.endDate,
      }).unwrap();
      
      setTripId(result.tripId);
      setCurrentStep(2);
    } catch (error) {
      console.error('Failed to create trip:', error);
    }
  };

  const handleStep2Submit = async () => {
    if (!tripId) return;
    
    try {
      await updatePreferences({
        tripId,
        preferences: tripData.preferences,
      }).unwrap();
      
      setCurrentStep(3);
    } catch (error) {
      console.error('Failed to update preferences:', error);
    }
  };

  const handleStep3Submit = async () => {
    if (!tripId) return;
    
    try {
      await updateCities({
        tripId,
        cities: tripData.cities,
      }).unwrap();
      
      onComplete(tripId);
    } catch (error) {
      console.error('Failed to update cities:', error);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      {/* Step Indicator */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          {[1, 2, 3].map((step) => (
            <div
              key={step}
              className={`flex items-center justify-center w-10 h-10 rounded-full ${
                step <= currentStep ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-600'
              }`}
            >
              {step}
            </div>
          ))}
        </div>
        <div className="flex justify-between mt-2 text-sm text-gray-600">
          <span>Basic Info</span>
          <span>Preferences</span>
          <span>Cities & Days</span>
        </div>
      </div>

      {/* Step Content */}
      {currentStep === 1 && (
        <TripBasicInfoForm
          data={tripData}
          onChange={setTripData}
          onSubmit={handleStep1Submit}
        />
      )}
      
      {currentStep === 2 && (
        <TripPreferencesForm
          data={tripData}
          onChange={setTripData}
          onSubmit={handleStep2Submit}
          onBack={() => setCurrentStep(1)}
        />
      )}
      
      {currentStep === 3 && (
        <TripCitiesForm
          data={tripData}
          onChange={setTripData}
          onSubmit={handleStep3Submit}
          onBack={() => setCurrentStep(2)}
        />
      )}
    </div>
  );
};
```

### 2. Search Components

```tsx
// components/Search/ActivitySearch.tsx
import React, { useState } from 'react';
import { useSearchActivitiesQuery } from '../services/tripPlanningService';
import { SearchResult } from '../types/trip';

interface ActivitySearchProps {
  tripId: string;
  city?: string;
  lastPlaceId?: string;
  onSelectActivity: (activity: SearchResult) => void;
}

const ActivitySearch: React.FC<ActivitySearchProps> = ({
  tripId,
  city,
  lastPlaceId,
  onSelectActivity,
}) => {
  const [query, setQuery] = useState('');
  const [maxResults, setMaxResults] = useState(10);

  const { data: searchResults, isLoading, error } = useSearchActivitiesQuery({
    tripId,
    query,
    city,
    lastPlaceId,
    maxResults,
  }, {
    skip: !tripId,
  });

  return (
    <div className="space-y-4">
      <div className="flex space-x-4">
        <input
          type="text"
          placeholder="Search activities..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
          value={maxResults}
          onChange={(e) => setMaxResults(Number(e.target.value))}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value={5}>5 results</option>
          <option value={10}>10 results</option>
          <option value={20}>20 results</option>
        </select>
      </div>

      {isLoading && (
        <div className="flex justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-600">Failed to load activities. Please try again.</p>
        </div>
      )}

      {searchResults && (
        <div className="grid gap-4 md:grid-cols-2">
          {searchResults.results.map((activity) => (
            <ActivityCard
              key={activity.placeId}
              activity={activity}
              onSelect={() => onSelectActivity(activity)}
            />
          ))}
        </div>
      )}
    </div>
  );
};

// components/Search/ActivityCard.tsx
const ActivityCard: React.FC<{ activity: SearchResult; onSelect: () => void }> = ({
  activity,
  onSelect,
}) => (
  <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
    <div className="flex justify-between items-start mb-2">
      <h3 className="font-semibold text-lg">{activity.name}</h3>
      {activity.rating && (
        <div className="flex items-center space-x-1">
          <span className="text-yellow-500">★</span>
          <span className="text-sm text-gray-600">{activity.rating}</span>
        </div>
      )}
    </div>
    
    <p className="text-gray-600 text-sm mb-3">{activity.formattedAddress}</p>
    
    <div className="flex flex-wrap gap-1 mb-3">
      {activity.types.slice(0, 3).map((type) => (
        <span
          key={type}
          className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full"
        >
          {type}
        </span>
      ))}
    </div>
    
    <div className="flex justify-between items-center">
      <span className="text-xs text-gray-500 capitalize">{activity.source}</span>
      <button
        onClick={onSelect}
        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Add to Trip
      </button>
    </div>
    
    {activity.distanceFromBias && (
      <p className="text-xs text-gray-500 mt-2">
        {activity.distanceFromBias.toFixed(1)} km from last location
      </p>
    )}
  </div>
);
```

## User Interface Examples

### 🎨 Trip Creation Wizard

```jsx
// pages/TripWizard.jsx
import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Stepper, Step, StepLabel, Box, Container } from '@mui/material';
import BasicTripForm from '../components/trip/TripCreation/BasicTripForm';
import PreferenceSelector from '../components/trip/TripCreation/PreferenceSelector';
import CitySelector from '../components/trip/TripCreation/CitySelector';
import TripSummary from '../components/trip/TripCreation/TripSummary';
import { 
  createBasicTrip, 
  updateTripPreferences, 
  updateTripCities,
  selectCreationStep,
  selectCurrentTrip,
  selectTripLoading 
} from '../store/slices/tripSlice';

const steps = ['Basic Info', 'Preferences', 'Cities', 'Summary'];

const TripWizard = () => {
  const dispatch = useDispatch();
  const creationStep = useSelector(selectCreationStep);
  const currentTrip = useSelector(selectCurrentTrip);
  const loading = useSelector(selectTripLoading);

  const getActiveStep = () => {
    switch (creationStep) {
      case 'basic': return 0;
      case 'preferences': return 1;
      case 'cities': return 2;
      case 'planning': return 3;
      default: return 0;
    }
  };

  const handleBasicTripSubmit = (tripData) => {
    dispatch(createBasicTrip(tripData));
  };

  const handlePreferencesSubmit = (preferences) => {
    dispatch(updateTripPreferences({ 
      tripId: currentTrip.tripId, 
      preferences 
    }));
  };

  const handleCitiesSubmit = (cityData) => {
    dispatch(updateTripCities({ 
      tripId: currentTrip.tripId, 
      cityData 
    }));
  };

  const renderStepContent = () => {
    switch (creationStep) {
      case 'basic':
        return (
          <BasicTripForm 
            onSubmit={handleBasicTripSubmit}
            loading={loading.creating}
          />
        );
      case 'preferences':
        return (
          <PreferenceSelector 
            tripId={currentTrip?.tripId}
            onSubmit={handlePreferencesSubmit}
            loading={loading.updating}
          />
        );
      case 'cities':
        return (
          <CitySelector 
            tripId={currentTrip?.tripId}
            onSubmit={handleCitiesSubmit}
            loading={loading.updating}
          />
        );
      case 'planning':
        return (
          <TripSummary 
            trip={currentTrip}
            loading={loading.updating}
          />
        );
      default:
        return null;
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Stepper activeStep={getActiveStep()} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>
      
      <Box>
        {renderStepContent()}
      </Box>
    </Container>
  );
};

export default TripWizard;
```

### 🔍 Search Results Component

```jsx
// components/trip/TripPlanning/ActivitySearch.jsx
import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { 
  Card, CardContent, CardMedia, Typography, Box, 
  TextField, Button, Grid, Chip, Rating, CircularProgress 
} from '@mui/material';
import { searchActivities, selectSearchResults, selectTripLoading } from '../../../store/slices/tripSlice';

const ActivitySearch = ({ tripId }) => {
  const dispatch = useDispatch();
  const searchResults = useSelector(selectSearchResults);
  const loading = useSelector(selectTripLoading);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCity, setSelectedCity] = useState('');

  const handleSearch = () => {
    dispatch(searchActivities({
      tripId,
      searchParams: {
        query: searchQuery,
        city: selectedCity,
        maxResults: 20
      }
    }));
  };

  const renderActivityCard = (activity) => (
    <Grid item xs={12} sm={6} md={4} key={activity.locationId}>
      <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {activity.images && activity.images[0] && (
          <CardMedia
            component="img"
            height="200"
            image={activity.images[0]}
            alt={activity.name}
          />
        )}
        <CardContent sx={{ flexGrow: 1 }}>
          <Typography gutterBottom variant="h6" component="h2">
            {activity.name}
          </Typography>
          
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <Rating value={activity.rating} readOnly size="small" />
            <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
              ({activity.reviewCount} reviews)
            </Typography>
          </Box>
          
          <Typography variant="body2" color="text.secondary" paragraph>
            {activity.description}
          </Typography>
          
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
            <Chip 
              label={activity.category} 
              size="small" 
              color="primary" 
              variant="outlined" 
            />
            {activity.subcategory && (
              <Chip 
                label={activity.subcategory} 
                size="small" 
                color="secondary" 
                variant="outlined" 
              />
            )}
          </Box>
          
          <Typography variant="body2" color="text.secondary">
            📍 {activity.address}
          </Typography>
          
          {activity.preferenceScore && (
            <Box sx={{ mt: 1 }}>
              <Typography variant="caption" color="primary">
                🎯 {(activity.preferenceScore * 100).toFixed(0)}% match
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>
    </Grid>
  );

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
        <TextField
          label="Search activities..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          fullWidth
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <TextField
          label="City (optional)"
          value={selectedCity}
          onChange={(e) => setSelectedCity(e.target.value)}
          sx={{ minWidth: 150 }}
        />
        <Button 
          variant="contained" 
          onClick={handleSearch}
          disabled={loading.searching}
          sx={{ minWidth: 100 }}
        >
          {loading.searching ? <CircularProgress size={20} /> : 'Search'}
        </Button>
      </Box>

      {loading.searching && (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      )}

      <Grid container spacing={3}>
        {searchResults.activities.map(renderActivityCard)}
      </Grid>

      {searchResults.activities.length === 0 && !loading.searching && (
        <Box sx={{ textAlign: 'center', my: 4 }}>
          <Typography variant="body1" color="text.secondary">
            No activities found. Try a different search term or city.
          </Typography>
        </Box>
      )}
    </Box>
  );
};

export default ActivitySearch;
```

## Category Management & Preference Mapping

### 🏷️ Frontend to Backend Preference Mapping

```javascript
// services/utils/preferenceMapper.js

// Frontend preference categories that users see
export const FRONTEND_PREFERENCES = {
  terrain: [
    { id: 'beach', label: '🏖️ Beach & Coast', keywords: ['beach', 'coast', 'ocean', 'sea'] },
    { id: 'mountain', label: '🏔️ Mountains & Hills', keywords: ['mountain', 'hill', 'peak', 'highlands'] },
    { id: 'urban', label: '🏙️ City & Urban', keywords: ['city', 'urban', 'downtown', 'metropolitan'] },
    { id: 'rural', label: '🌾 Rural & Countryside', keywords: ['rural', 'countryside', 'village', 'farm'] },
    { id: 'forest', label: '🌲 Forest & Jungle', keywords: ['forest', 'jungle', 'woodland', 'trees'] },
    { id: 'desert', label: '🏜️ Desert & Arid', keywords: ['desert', 'arid', 'sand', 'dry'] }
  ],
  
  activities: [
    { 
      id: 'adventure', 
      label: '🏃 Adventure & Sports', 
      keywords: ['adventure', 'sport', 'extreme', 'hiking', 'climbing'],
      backendCategories: ['sports', 'outdoor', 'adventure', 'recreation']
    },
    { 
      id: 'culture', 
      label: '🏛️ Culture & History', 
      keywords: ['culture', 'history', 'museum', 'heritage', 'art'],
      backendCategories: ['museums', 'historical', 'cultural', 'arts']
    },
    { 
      id: 'nightlife', 
      label: '🌃 Nightlife & Entertainment', 
      keywords: ['nightlife', 'bar', 'club', 'entertainment', 'party'],
      backendCategories: ['nightlife', 'entertainment', 'bars', 'clubs']
    },
    { 
      id: 'nature', 
      label: '🌿 Nature & Wildlife', 
      keywords: ['nature', 'wildlife', 'park', 'garden', 'eco'],
      backendCategories: ['nature', 'parks', 'wildlife', 'outdoor']
    },
    { 
      id: 'food', 
      label: '🍽️ Food & Dining', 
      keywords: ['food', 'restaurant', 'dining', 'cuisine', 'culinary'],
      backendCategories: ['restaurants', 'food', 'dining', 'culinary']
    },
    { 
      id: 'shopping', 
      label: '🛍️ Shopping & Markets', 
      keywords: ['shopping', 'market', 'mall', 'store', 'retail'],
      backendCategories: ['shopping', 'markets', 'retail', 'stores']
    },
    { 
      id: 'relaxation', 
      label: '🧘 Relaxation & Wellness', 
      keywords: ['spa', 'wellness', 'relaxation', 'meditation', 'health'],
      backendCategories: ['spas', 'wellness', 'health', 'relaxation']
    }
  ]
};

// Map frontend preferences to backend search categories
export const mapPreferencesToCategories = (frontendPreferences) => {
  const categories = new Set();
  
  frontendPreferences.forEach(prefId => {
    const preference = FRONTEND_PREFERENCES.activities.find(p => p.id === prefId);
    if (preference && preference.backendCategories) {
      preference.backendCategories.forEach(cat => categories.add(cat));
    }
  });
  
  return Array.from(categories);
};

// Generate search keywords from preferences
export const generateSearchKeywords = (preferences) => {
  const keywords = new Set();
  
  [...(preferences.terrain || []), ...(preferences.activities || [])].forEach(prefId => {
    // Check terrain preferences
    const terrainPref = FRONTEND_PREFERENCES.terrain.find(p => p.id === prefId);
    if (terrainPref) {
      terrainPref.keywords.forEach(keyword => keywords.add(keyword));
    }
    
    // Check activity preferences
    const activityPref = FRONTEND_PREFERENCES.activities.find(p => p.id === prefId);
    if (activityPref) {
      activityPref.keywords.forEach(keyword => keywords.add(keyword));
    }
  });
  
  return Array.from(keywords);
};

// Score location based on preference match
export const calculatePreferenceScore = (location, userPreferences) => {
  let score = 0;
  let maxScore = 0;
  
  const keywords = generateSearchKeywords(userPreferences);
  const categories = mapPreferencesToCategories(userPreferences.activities || []);
  
  // Check category matches
  if (categories.includes(location.category?.toLowerCase())) {
    score += 3;
  }
  maxScore += 3;
  
  if (categories.includes(location.subcategory?.toLowerCase())) {
    score += 2;
  }
  maxScore += 2;
  
  // Check keyword matches in name and description
  const text = `${location.name} ${location.description}`.toLowerCase();
  keywords.forEach(keyword => {
    if (text.includes(keyword.toLowerCase())) {
      score += 1;
    }
    maxScore += 1;
  });
  
  return maxScore > 0 ? score / maxScore : 0;
};

export default {
  FRONTEND_PREFERENCES,
  mapPreferencesToCategories,
  generateSearchKeywords,
  calculatePreferenceScore
};
```

## Error Handling & Validation

### 🚨 Comprehensive Error Handling

```javascript
// services/utils/errorHandler.js
export class TripPlanningError extends Error {
  constructor(message, code, details = null) {
    super(message);
    this.name = 'TripPlanningError';
    this.code = code;
    this.details = details;
  }
}

export const ERROR_CODES = {
  UNAUTHORIZED: 'UNAUTHORIZED',
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  NETWORK_ERROR: 'NETWORK_ERROR',
  NOT_FOUND: 'NOT_FOUND',
  SERVER_ERROR: 'SERVER_ERROR',
  TRIP_NOT_FOUND: 'TRIP_NOT_FOUND',
  INVALID_DATES: 'INVALID_DATES',
  NO_RESULTS: 'NO_RESULTS'
};

export const handleApiError = (error, context = '') => {
  console.error(`API Error in ${context}:`, error);
  
  if (error.response) {
    const { status, data } = error.response;
    
    switch (status) {
      case 401:
        return new TripPlanningError(
          'Please log in to continue',
          ERROR_CODES.UNAUTHORIZED,
          data
        );
      case 400:
        return new TripPlanningError(
          data.message || 'Invalid request',
          ERROR_CODES.VALIDATION_ERROR,
          data
        );
      case 404:
        return new TripPlanningError(
          'Resource not found',
          ERROR_CODES.NOT_FOUND,
          data
        );
      case 500:
        return new TripPlanningError(
          'Server error. Please try again later.',
          ERROR_CODES.SERVER_ERROR,
          data
        );
      default:
        return new TripPlanningError(
          `Unexpected error (${status})`,
          ERROR_CODES.SERVER_ERROR,
          data
        );
    }
  } else if (error.request) {
    return new TripPlanningError(
      'Network error. Please check your connection.',
      ERROR_CODES.NETWORK_ERROR,
      error.request
    );
  } else {
    return new TripPlanningError(
      error.message || 'Unknown error',
      ERROR_CODES.SERVER_ERROR,
      error
    );
  }
};

// React hook for error handling
import { useState } from 'react';

export const useErrorHandler = () => {
  const [error, setError] = useState(null);

  const handleError = (error, context = '') => {
    const processedError = handleApiError(error, context);
    setError(processedError);
    return processedError;
  };

  const clearError = () => setError(null);

  return { error, handleError, clearError };
};
```

### ✅ Input Validation

```javascript
// services/utils/validation.js
import { format, isAfter, isBefore, addDays } from 'date-fns';

export const validateTripBasic = (tripData) => {
  const errors = {};

  // Trip name validation
  if (!tripData.tripName || tripData.tripName.trim().length < 3) {
    errors.tripName = 'Trip name must be at least 3 characters long';
  }

  if (tripData.tripName && tripData.tripName.length > 100) {
    errors.tripName = 'Trip name must be less than 100 characters';
  }

  // Date validation
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (!tripData.startDate) {
    errors.startDate = 'Start date is required';
  } else {
    const startDate = new Date(tripData.startDate);
    if (isBefore(startDate, today)) {
      errors.startDate = 'Start date cannot be in the past';
    }
  }

  if (!tripData.endDate) {
    errors.endDate = 'End date is required';
  } else if (tripData.startDate) {
    const startDate = new Date(tripData.startDate);
    const endDate = new Date(tripData.endDate);
    
    if (!isAfter(endDate, startDate)) {
      errors.endDate = 'End date must be after start date';
    }

    // Check for reasonable trip duration
    const maxDate = addDays(startDate, 365); // Max 1 year
    if (isAfter(endDate, maxDate)) {
      errors.endDate = 'Trip cannot be longer than 1 year';
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};

export const validatePreferences = (preferences) => {
  const errors = {};

  if (!preferences.terrainPreferences || preferences.terrainPreferences.length === 0) {
    if (!preferences.activityPreferences || preferences.activityPreferences.length === 0) {
      errors.preferences = 'Please select at least one terrain or activity preference';
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};

export const validateCities = (cityData) => {
  const errors = {};

  if (!cityData.cities || cityData.cities.length === 0) {
    errors.cities = 'Please select at least one city';
  }

  if (!cityData.cityDays || Object.keys(cityData.cityDays).length === 0) {
    errors.cityDays = 'Please specify days for each city';
  } else {
    // Validate that all cities have day allocations
    cityData.cities?.forEach(city => {
      if (!cityData.cityDays[city] || cityData.cityDays[city] < 1) {
        errors.cityDays = `Please specify at least 1 day for ${city}`;
      }
    });
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

## Testing Strategy

### 🧪 Unit Testing for API Service

```javascript
// services/api/__tests__/tripService.test.js
import { tripService } from '../tripService';
import axios from 'axios';

jest.mock('axios');
const mockedAxios = axios;

describe('TripService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createBasicTrip', () => {
    it('should create a basic trip successfully', async () => {
      const mockResponse = {
        data: {
          message: 'Basic trip created successfully',
          tripId: 'trip-123',
          trip: { tripId: 'trip-123', tripName: 'Test Trip' }
        }
      };

      mockedAxios.post.mockResolvedValue(mockResponse);

      const tripData = {
        tripName: 'Test Trip',
        startDate: '2024-12-01',
        endDate: '2024-12-07'
      };

      const result = await tripService.createBasicTrip(tripData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/create-basic', tripData);
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle API errors', async () => {
      const mockError = {
        response: {
          status: 400,
          data: { error: 'Bad Request', message: 'Invalid trip data' }
        }
      };

      mockedAxios.post.mockRejectedValue(mockError);

      await expect(tripService.createBasicTrip({})).rejects.toThrow();
    });
  });

  describe('searchActivities', () => {
    it('should search activities with correct parameters', async () => {
      const mockResponse = {
        data: {
          message: 'Activities found',
          results: [
            { locationId: '1', name: 'Test Activity' }
          ]
        }
      };

      mockedAxios.get.mockResolvedValue(mockResponse);

      const searchParams = {
        query: 'hiking',
        city: 'Colombo',
        maxResults: 10
      };

      const result = await tripService.searchActivities('trip-123', searchParams);

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/trip-123/search/activities?query=hiking&city=Colombo&maxResults=10'
      );
      expect(result).toEqual(mockResponse.data);
    });
  });
});
```

### 🎭 Component Testing

```javascript
// components/trip/TripCreation/__tests__/BasicTripForm.test.jsx
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import BasicTripForm from '../BasicTripForm';

const theme = createTheme();

const renderWithTheme = (component) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe('BasicTripForm', () => {
  const mockOnSubmit = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders form elements correctly', () => {
    renderWithTheme(<BasicTripForm onSubmit={mockOnSubmit} />);

    expect(screen.getByLabelText(/trip name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/start date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/end date/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create trip/i })).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    renderWithTheme(<BasicTripForm onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByRole('button', { name: /create trip/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/trip name is required/i)).toBeInTheDocument();
    });

    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('submits valid form data', async () => {
    renderWithTheme(<BasicTripForm onSubmit={mockOnSubmit} />);

    const tripNameInput = screen.getByLabelText(/trip name/i);
    fireEvent.change(tripNameInput, { target: { value: 'My Test Trip' } });

    // Mock date picker interactions
    // (Implementation depends on your date picker library)

    const submitButton = screen.getByRole('button', { name: /create trip/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        tripName: 'My Test Trip',
        startDate: expect.any(String),
        endDate: expect.any(String)
      });
    });
  });
});
```

---

## 🚀 Quick Start Integration Guide

### 1. Install Dependencies
```bash
npm install @reduxjs/toolkit react-redux axios @mui/material @mui/icons-material date-fns
```

### 2. Setup Redux Store
```javascript
// store/store.js
import { configureStore } from '@reduxjs/toolkit';
import tripReducer from './slices/tripSlice';

export const store = configureStore({
  reducer: {
    trip: tripReducer
  }
});
```

### 3. Wrap Your App
```jsx
// App.jsx
import { Provider } from 'react-redux';
import { store } from './store/store';
import TripWizard from './pages/TripWizard';

function App() {
  return (
    <Provider store={store}>
      <TripWizard />
    </Provider>
  );
}
```

### 4. Start Using the Components
```jsx
// Your component
import { useDispatch } from 'react-redux';
import { createBasicTrip } from './store/slices/tripSlice';

const MyComponent = () => {
  const dispatch = useDispatch();
  
  const handleCreateTrip = (tripData) => {
    dispatch(createBasicTrip(tripData));
  };
  
  return <BasicTripForm onSubmit={handleCreateTrip} />;
};
```

---

This comprehensive guide provides everything needed to integrate the trip planning service with your frontend application. The examples show real-world implementation patterns, proper error handling, and testing strategies that will ensure a robust and user-friendly trip planning experience.
