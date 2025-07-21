# Pooling Service Client Endpoints Documentation

This document outlines the external service endpoints that the Pooling Service communicates with via REST clients.

## Trip Planning Service Client

The `TripServiceClient` communicates with the Trip Planning Service microservice to fetch trip details and manage trip data.

**Base URL**: `http://localhost:8082` (configurable via `services.trip-service.base-url`)

### 1. Get Trip Details

**Endpoint**: `GET /api/v1/itinerary/{tripId}?userId={userId}`

**Purpose**: Retrieves complete trip plan details including daily plans, cities, and attractions

**Parameters**:
- `tripId` (Path): The unique trip ID
- `userId` (Query): User ID for authentication and authorization

**Example Request**:
```http
GET /api/v1/itinerary/trip_001?userId=user_789
```

**Expected Response Structure**:
```json
{
  "status": "success",
  "tripId": "trip_001",
  "tripName": "Sri Lanka Adventure",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15",
  "baseCity": "Colombo",
  "budgetLevel": "Medium",
  "activityPacing": "Normal",
  "preferredActivities": ["Hiking", "Cultural Tours"],
  "preferredTerrains": ["Beach", "Mountain"],
  "dailyPlans": [
    {
      "day": 1,
      "city": "Colombo",
      "attractions": [
        {
          "name": "Gangaramaya Temple",
          "userSelected": true
        }
      ]
    }
  ]
}
```

**Usage in Pooling Service**:
- Called by `GroupService.convertToEnhancedPublicGroupResponse()` to fetch trip details for group display
- Used to extract cities, attractions, dates, and trip preferences for enhanced group information

---

### 2. Update City for Day

**Endpoint**: `POST /api/v1/itinerary/{tripId}/day/{day}/city`

**Purpose**: Updates the city for a specific day in a trip itinerary

**Parameters**:
- `tripId` (Path): The unique trip ID
- `day` (Path): Day number (1-based, 1-30)

**Request Body**:
```json
{
  "userId": "user_789",
  "city": "Kandy"
}
```

**Example Request**:
```http
POST /api/v1/itinerary/trip_001/day/2/city
Content-Type: application/json

{
  "userId": "user_789",
  "city": "Kandy"
}
```

**Expected Response**:
```json
{
  "status": "success",
  "message": "City updated successfully for day 2",
  "tripId": "trip_001",
  "day": 2,
  "city": "Kandy"
}
```

**Usage in Pooling Service**:
- Available via `TripServiceClient.updateCityForDay()` method
- Can be used to update trip destinations programmatically

---

### 3. Add Place to Itinerary

**Endpoint**: `POST /api/v1/itinerary/{tripId}/day/{day}/{type}?userId={userId}`

**Purpose**: Adds a selected place (attraction, hotel, restaurant) to a specific day in the trip

**Parameters**:
- `tripId` (Path): The unique trip ID
- `day` (Path): Day number (1-based, 1-30)
- `type` (Path): Type of place - `attractions`, `hotels`, or `restaurants`
- `userId` (Query): User ID for authentication

**Request Body** (SuggestionResponse format):
```json
{
  "id": "place_123",
  "name": "Sigiriya Rock Fortress",
  "address": "Sigiriya, Sri Lanka",
  "price": "$25",
  "priceLevel": "Medium",
  "category": "Cultural",
  "rating": 4.8,
  "reviews": 2847,
  "popularityLevel": "High",
  "image": "https://example.com/sigiriya.jpg",
  "latitude": 7.9568,
  "longitude": 80.7608,
  "distanceKm": 15.2,
  "isOpenNow": true,
  "source": "TripAdvisor",
  "googlePlaceId": "google_place_456",
  "isRecommended": true
}
```

**Example Request**:
```http
POST /api/v1/itinerary/trip_001/day/3/attractions?userId=user_789
Content-Type: application/json

{
  "id": "place_123",
  "name": "Sigiriya Rock Fortress",
  "address": "Sigiriya, Sri Lanka",
  "price": "$25",
  "priceLevel": "Medium",
  "category": "Cultural",
  "rating": 4.8,
  "reviews": 2847,
  "popularityLevel": "High",
  "image": "https://example.com/sigiriya.jpg",
  "latitude": 7.9568,
  "longitude": 80.7608,
  "distanceKm": 15.2,
  "isOpenNow": true,
  "source": "TripAdvisor",
  "googlePlaceId": "google_place_456",
  "isRecommended": true
}
```

**Expected Response**:
```json
{
  "status": "success",
  "message": "Place added to itinerary successfully",
  "tripId": "trip_001",
  "day": 3,
  "type": "attractions",
  "placeName": "Sigiriya Rock Fortress"
}
```

**Usage in Pooling Service**:
- Available via `TripServiceClient.addPlaceToItinerary()` method
- Can be used to programmatically add places to group trips

---

## User Service Client

The `UserServiceClient` communicates with the User Services microservice to fetch user profile information.

**Base URL**: `http://localhost:8083` (configurable via `services.user-service.base-url`)

### 1. Get User Profile by Email

**Endpoint**: `GET /api/v1/tourist/profile?email={email}`

**Purpose**: Retrieves user profile information including name and nationality

**Parameters**:
- `email` (Query): User's email address

**Example Request**:
```http
GET /api/v1/tourist/profile?email=john.doe@example.com
```

**Expected Response**:
```json
{
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "nationality": "American"
}
```

**Usage in Pooling Service**:
- Called by `UserServiceClient.getUserByEmail()` and `UserServiceClient.getUserNameByEmail()`
- Used by `GroupService.getCreatorName()` to resolve user names for group display
- Provides fallback to email if full name is not available

---

## Frontend Integration Examples

Based on the React frontend code provided, here are the request body formats that should be used:

### Trip Planning Service Integration

**Frontend to Backend Place Addition**:
```javascript
// Frontend transforms place data to backend format
const placeData = {
  id: place.googlePlaceId || place.id || `place_${Date.now()}`,
  name: place.name,
  address: place.address || place.location,
  price: place.price || place.priceRange,
  priceLevel: place.priceLevel || 'Medium',
  category: place.category || place.type || 'Activity',
  rating: place.rating || 0,
  reviews: place.reviews || 0,
  popularityLevel: place.popularityLevel || 'Medium',
  image: place.image || 'https://via.placeholder.com/400x300',
  latitude: place.latitude || 0,
  longitude: place.longitude || 0,
  distanceKm: place.distanceKm || 0,
  isOpenNow: place.isOpenNow !== undefined ? place.isOpenNow : true,
  source: place.source || 'Frontend',
  googlePlaceId: place.googlePlaceId || place.id,
  isRecommended: place.isRecommended || false
};

// API Call
const response = await fetch(`${API_BASE_URL}/itinerary/${tripId}/day/${dayNumber}/${apiType}?userId=${userUid}`, {
  method: 'POST',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(placeData)
});
```

### City Update Integration

**Frontend to Backend City Update**:
```javascript
// Frontend city update
const updateCityData = {
  userId: userUid,
  city: selectedCity.name
};

const response = await fetch(`${API_BASE_URL}/itinerary/${tripId}/day/${dayNumber}/city`, {
  method: 'POST',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(updateCityData)
});
```

---

## Configuration Properties

Add these properties to `application.yml` for service client configuration:

```yaml
services:
  trip-service:
    base-url: http://localhost:8082
  user-service:
    base-url: http://localhost:8083
```

---

## Error Handling

All client methods include comprehensive error handling:

1. **404 Not Found**: Trip or user not found
2. **403 Forbidden**: Unauthorized access to trip
3. **400 Bad Request**: Invalid parameters or validation errors
4. **500 Internal Server Error**: Unexpected server errors

Clients log appropriate warnings and errors while gracefully degrading functionality when external services are unavailable.

---

## Dependencies

The pooling service includes these REST client dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

RestTemplate is configured as a bean for HTTP communication with external services.
