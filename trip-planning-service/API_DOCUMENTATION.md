# Trip Planning Service - Complete API Documentation

## Overview
The Trip Planning Service provides comprehensive trip management functionality including location search, contextual suggestions, day planning, and real-time recommendations. All endpoints require authentication and include userId support for microservice coordination.

## Base URL
```
http://localhost:8083/api/v1/trip
```

## Authentication
All endpoints require a valid session. Include credentials in requests:
```javascript
fetch(url, {
  method: 'GET/POST',
  credentials: 'include',
  headers: { 'Content-Type': 'application/json' }
});
```

## Health Check Endpoints

### 1. Service Health Check
```
GET /health
```
**Response:**
```
"Trip Planning Service is running"
```

### 2. Spring Boot Actuator Health
```
GET /actuator/health
```

---

## Trip Management Endpoints

### 1. Create Basic Trip
```
POST /create-basic
```
**Request Body:**
```json
{
  "userId": "string",
  "tripName": "string",
  "startDate": "2025-01-15",
  "endDate": "2025-01-20"
}
```
**Response:**
```json
{
  "message": "Basic trip created successfully",
  "tripId": "string",
  "trip": {
    "tripId": "string",
    "tripName": "string",
    "userId": "string",
    "startDate": "2025-01-15",
    "endDate": "2025-01-20",
    "status": "DRAFT"
  }
}
```

### 2. Create Trip with Preferences
```
POST /initiate
```
**Request Body:**
```json
{
  "userId": "string",
  "tripName": "string",
  "startDate": "2025-01-15",
  "endDate": "2025-01-20",
  "preferences": {
    "budget": "MEDIUM",
    "travelStyle": "ADVENTURE",
    "interests": ["NATURE", "CULTURE"]
  }
}
```

### 3. Update Trip Preferences
```
POST /{tripId}/preferences
```
**Request Body:**
```json
{
  "userId": "string",
  "preferences": {
    "budget": "MEDIUM",
    "travelStyle": "ADVENTURE",
    "interests": ["NATURE", "CULTURE", "FOOD"]
  }
}
```

### 4. Update Trip Cities
```
POST /{tripId}/cities
```
**Request Body:**
```json
{
  "userId": "string",
  "cities": ["Colombo", "Kandy", "Galle"],
  "cityDays": {
    "Colombo": 2,
    "Kandy": 2,
    "Galle": 3
  }
}
```

### 5. Get User's Trips
```
GET /my-trips
```
**Response:**
```json
{
  "trips": [...],
  "userId": "string",
  "count": 5
}
```

### 6. Get Trip Summary
```
GET /{tripId}/summary
```

### 7. Get Trip Map Data
```
GET /{tripId}/map-data
```

---

## Location Search Endpoints

### 1. Search Locations
```
GET /search-locations?query={query}&city={city}&maxResults={n}
```
**Parameters:**
- `query` (required): Search term
- `city` (optional): Filter by city
- `biasLat` (optional): Latitude for location bias
- `biasLng` (optional): Longitude for location bias
- `maxResults` (optional): Max results (default: 10, max: 50)

**Response:**
```json
{
  "results": [
    {
      "placeId": "string",
      "name": "string",
      "formattedAddress": "string",
      "latitude": 6.9271,
      "longitude": 79.8612,
      "rating": 4.5,
      "types": ["tourist_attraction"]
    }
  ],
  "count": 10,
  "query": "Colombo"
}
```

### 2. Contextual Location Search
```
GET /{tripId}/contextual-search?query={query}&placeType={type}&dayNumber={day}
```
**Parameters:**
- `query` (required): Search term
- `placeType` (optional): Filter by place type
- `dayNumber` (optional): Specific day context
- `lastPlaceId` (optional): Previous place for proximity
- `maxResults` (optional): Max results

### 3. Validate Place
```
POST /validate-place
```
**Request Body:**
```json
{
  "placeName": "string",
  "city": "string",
  "placeType": "ATTRACTION"
}
```

### 4. Get Place Details
```
GET /place-details/{placeId}
```

### 5. Get Place Categories
```
GET /place-categories
```

---

## Day Planning Endpoints

### 1. Get Day Plan
```
GET /{tripId}/day/{day}
```

### 2. Get Day Plan with Suggestions
```
GET /{tripId}/day/{dayNumber}/plan
```

### 3. Add Place to Trip
```
POST /{tripId}/add-place
```
**Request Body:**
```json
{
  "userId": "string",
  "placeName": "string",
  "placeType": "ATTRACTION",
  "city": "string",
  "latitude": 6.9271,
  "longitude": 79.8612
}
```

### 4. Add Place to Specific Day
```
POST /{tripId}/day/{dayNumber}/add-place
```
**Request Body:**
```json
{
  "userId": "string",
  "placeName": "string",
  "placeType": "ATTRACTION",
  "dayNumber": 1,
  "city": "string",
  "latitude": 6.9271,
  "longitude": 79.8612,
  "previousPlaceId": "string"
}
```

### 5. Quick Add Place
```
POST /{tripId}/day/{dayNumber}/quick-add?placeId={id}&placeName={name}&placeType={type}
```

---

## Suggestion Endpoints

### 1. Get Trip Suggestions
```
GET /{tripId}/suggestions?day={dayNumber}
```

### 2. Get Contextual Suggestions
```
GET /{tripId}/day/{dayNumber}/contextual-suggestions?contextType={type}
```

### 3. Get Nearby Suggestions
```
GET /{tripId}/nearby-suggestions?placeId={id}&placeType={type}&maxResults={n}
```

### 4. Get Realtime Suggestions
```
GET /{tripId}/day/{dayNumber}/realtime-suggestions?lastPlaceId={id}&category={cat}
```

---

## Search by Category Endpoints

### 1. Search Activities
```
GET /{tripId}/search/activities?query={query}&city={city}&lastPlaceId={id}&maxResults={n}&userId={userId}
```

### 2. Search Accommodation
```
GET /{tripId}/search/accommodation?query={query}&city={city}&lastPlaceId={id}&maxResults={n}&userId={userId}
```

### 3. Search Dining
```
GET /{tripId}/search/dining?query={query}&city={city}&lastPlaceId={id}&maxResults={n}&userId={userId}
```

---

## Travel Information Endpoints

### 1. Get Travel Info
```
GET /{tripId}/travel-info?fromPlaceId={from}&toPlaceId={to}
```

### 2. Get Enhanced Travel Info
```
GET /{tripId}/enhanced-travel-info?fromPlaceId={from}&toPlaceId={to}
```

### 3. Optimize Trip Order
```
POST /{tripId}/optimize-order
```

---

## Frontend Integration Examples

### Creating a Trip
```javascript
const createTrip = async (tripData, userId) => {
  const response = await fetch('/api/v1/trip/create-basic', {
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
```

### Searching for Cities
```javascript
const searchCities = async (query) => {
  const response = await fetch(`/api/v1/trip/search-locations?query=${encodeURIComponent(query)}`, {
    method: 'GET',
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Search failed: ${response.status}`);
  }
  
  const result = await response.json();
  return result.results;
};
```

### Adding Cities to Trip
```javascript
const updateCities = async (tripId, cityData, userId) => {
  const response = await fetch(`/api/v1/trip/${tripId}/cities`, {
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
```

### Searching for Popular Destinations
```javascript
const searchPopularDestinations = async (country = "Sri Lanka") => {
  const response = await fetch(`/api/v1/trip/search-locations?query=${encodeURIComponent(country)}`, {
    method: 'GET',
    credentials: 'include'
  });
  
  if (!response.ok) {
    throw new Error(`Search failed: ${response.status}`);
  }
  
  const result = await response.json();
  return result.results.filter(place => 
    place.types.includes('locality') || 
    place.types.includes('administrative_area_level_1')
  );
};
```

---

## Error Handling

All endpoints return consistent error responses:

```json
{
  "error": "Error type",
  "message": "Detailed error message"
}
```

**Common HTTP Status Codes:**
- `200` - Success
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (session required)
- `404` - Not Found
- `500` - Internal Server Error

---

## Request/Response Models

### Place Types
- `ATTRACTION`
- `RESTAURANT`
- `HOTEL`
- `ACTIVITY`

### Budget Types
- `LOW`
- `MEDIUM`
- `HIGH`

### Travel Styles
- `ADVENTURE`
- `CULTURAL`
- `RELAXATION`
- `BUSINESS`

### Interest Categories
- `NATURE`
- `CULTURE`
- `FOOD`
- `ADVENTURE`
- `HISTORY`
- `SHOPPING`
- `NIGHTLIFE`
