# Frontend Integration Guide - Trip Planning Service

This document provides integration details for the trip planning service API endpoints.

## Base URL
```
http://localhost:8080/v1/itinerary
```

## Authentication
Authentication is handled through the `userId` field in request bodies. No separate headers are required.

## CORS Configuration
The service is configured to accept requests from:
- `http://localhost:3000` (React development server)
- `http://127.0.0.1:3000`
- Any `localhost` origin on ports 3000-3009

## Endpoints

### 1. Create Trip Itinerary
**POST** `/initiate`

Creates a new trip plan with empty daily plans for the specified date range.

#### Request Body
```json
{
  "userId": "user123",
  "tripName": "My Amazing Trip",
  "startDate": "2024-07-15",
  "endDate": "2024-07-20",
  "arrivalTime": "10:30",
  "baseCity": "Colombo",
  "multiCityAllowed": true,
  "activityPacing": "Normal",
  "budgetLevel": "Medium",
  "preferredTerrains": ["Beach", "Mountain"],
  "preferredActivities": ["Sightseeing", "Adventure"]
}
```

#### Response (Success - 201 Created)
```json
{
  "status": "success",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Trip created successfully"
}
```

#### Response (Error - 400 Bad Request)
```json
{
  "status": "error",
  "tripId": null,
  "message": "Start date must be before or equal to end date"
}
```

### 2. Update City for Specific Day
**POST** `/{tripId}/day/{day}/city`

Updates the city for a specific day in a trip plan's daily plans.

#### Headers
```
Content-Type: application/json
```

#### Path Parameters
- `tripId`: The UUID of the trip to update
- `day`: The day number to update (1-based indexing)

#### Request Body
```json
{
  "userId": "user123",
  "city": "Kandy"
}
```

#### Response (Success - 200 OK)
```json
{
  "status": "success",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "day": 2,
  "city": "Kandy",
  "message": "City updated successfully"
}
```

#### Error Responses

**Trip Not Found (404 Not Found)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "Trip not found with ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Unauthorized Access (403 Forbidden)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "You are not authorized to modify this trip"
}
```

**Invalid Day Number (400 Bad Request)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "Day 10 is invalid. Trip has only 5 days"
}
```

**Validation Error (400 Bad Request)**
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": {
    "city": "City cannot be blank"
  }
}
```

## Frontend Usage Examples

### JavaScript/React Examples

#### 1. Create a Trip
```javascript
const createTrip = async (tripData) => {
  try {
    const response = await fetch('http://localhost:8080/v1/itinerary/initiate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(tripData)
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('Trip created:', result.tripId);
      return result;
    } else {
      console.error('Error creating trip:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
    throw error;
  }
};
```

#### 2. Update City for a Day
```javascript
const updateCity = async (tripId, day, userId, city) => {
  try {
    const response = await fetch(`http://localhost:8080/v1/itinerary/${tripId}/day/${day}/city`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userId, city })
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('City updated successfully:', result);
      return result;
    } else {
      console.error('Error updating city:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
    throw error;
  }
};

// Usage
updateCity('550e8400-e29b-41d4-a716-446655440000', 2, 'user123', 'Kandy')
  .then(result => {
    // Handle success
    console.log(`Day ${result.day} city updated to ${result.city}`);
  })
  .catch(error => {
    // Handle error
    console.error('Failed to update city:', error.message);
  });
```

#### 3. Error Handling in React Component
```javascript
const TripDayEditor = ({ tripId, day, userId }) => {
  const [city, setCity] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleUpdateCity = async () => {
    if (!city.trim()) {
      setError('City cannot be empty');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const result = await updateCity(tripId, day, userId, city.trim());
      // Handle success - maybe show a toast notification
      console.log('City updated successfully');
    } catch (error) {
      setError(error.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <input
        type="text"
        value={city}
        onChange={(e) => setCity(e.target.value)}
        placeholder="Enter city name"
        disabled={isLoading}
      />
      <button onClick={handleUpdateCity} disabled={isLoading}>
        {isLoading ? 'Updating...' : 'Update City'}
      </button>
      {error && <div className="error">{error}</div>}
    </div>
  );
};
```

## Testing with curl

### Create a Trip
```bash
curl -X POST http://localhost:8080/v1/itinerary/initiate \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "userId": "user123",
    "tripName": "Test Trip",
    "startDate": "2024-07-15",
    "endDate": "2024-07-17",
    "baseCity": "Colombo"
  }'
```

### Update City for Day 2
```bash
curl -X POST http://localhost:8080/v1/itinerary/550e8400-e29b-41d4-a716-446655440000/day/2/city \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "city": "Kandy"}'
```

## Notes

1. **User ID**: Always include the `userId` field in request bodies for authentication and authorization.

2. **CORS**: The service is configured for local development. For production, update the CORS configuration accordingly.

3. **Day Indexing**: Day numbers are 1-based (day 1, day 2, etc.), not 0-based.

4. **Error Handling**: Always check the HTTP status code and the `status` field in the response.

5. **Validation**: Both `userId` and `city` fields in update requests cannot be blank or empty.

6. **Database Updates**: The update endpoint uses MongoDB array updates for efficient partial document updates.

7. **Timestamps**: The `lastUpdated` timestamp is automatically updated when a city is changed.

8. **User Selection Flag**: When a city is updated, the `userSelected` flag for that day is automatically set to `true`.
