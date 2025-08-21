# Get User Trips Endpoint - Testing Guide

## Endpoint Details
- **Method**: `GET`
- **URL**: `http://localhost:8084/api/v1/itinerary`
- **Purpose**: Retrieves all trips for a specific user
- **Authentication**: User ID passed as query parameter

## Request Format

### Query Parameters
- `userId` (required): String - The user's unique identifier

### Example Request
```
GET http://localhost:8084/api/v1/itinerary?userId=wBuieMHjt1RKKgRoDgI9v6VyNHF3
```

## Expected Response Format

### Success Response (200 OK)
```json
[
  {
    "status": "success",
    "tripId": "trip_abc123",
    "tripName": "Sri Lanka Adventure",
    "startDate": "2025-08-10",
    "endDate": "2025-08-15",
    "baseCity": "Colombo",
    "message": null
  },
  {
    "status": "success", 
    "tripId": "trip_def456",
    "tripName": "Cultural Journey",
    "startDate": "2025-09-01",
    "endDate": "2025-09-05",
    "baseCity": "Kandy",
    "message": null
  }
]
```

### Empty Response (200 OK)
```json
[]
```

### Error Response (400 Bad Request)
```json
{
  "status": "error",
  "tripId": null,
  "message": "User ID cannot be blank or null"
}
```

### Error Response (500 Internal Server Error)
```json
{
  "status": "error",
  "tripId": null,
  "message": "Failed to retrieve trips. Please try again later."
}
```

## Test Cases Included

### 1. Success Cases
- **Valid User ID**: Tests with existing Firebase user ID
- **Different User ID**: Tests with alternative user ID
- **Load Test**: Tests with randomly generated user IDs

### 2. Validation Error Cases
- **Missing UserId Parameter**: Tests without userId query parameter
- **Empty UserId**: Tests with empty string userId
- **Null UserId**: Tests with "null" as userId value

### 3. Edge Cases
- **Very Long UserId**: Tests system limits with extremely long user ID
- **Special Characters**: Tests with special characters in user ID
- **Invalid HTTP Method**: Tests POST method on GET endpoint

### 4. Infrastructure Tests
- **Service Unavailable**: Tests wrong port to simulate service down
- **Performance Test**: Measures response times and validates acceptable performance

## Frontend Integration

### JavaScript Example
```javascript
// Function to get user trips
async function getUserTrips(userId) {
    try {
        const response = await fetch(`http://localhost:8084/api/v1/itinerary?userId=${encodeURIComponent(userId)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const trips = await response.json();
        return trips;
    } catch (error) {
        console.error('Error fetching user trips:', error);
        throw error;
    }
}

// Usage example
getUserTrips('wBuieMHjt1RKKgRoDgI9v6VyNHF3')
    .then(trips => {
        console.log('User trips:', trips);
        // Handle the trips array
        trips.forEach(trip => {
            console.log(`Trip: ${trip.tripName} (${trip.startDate} to ${trip.endDate})`);
        });
    })
    .catch(error => {
        console.error('Failed to get trips:', error);
        // Handle error (show user message, etc.)
    });
```

### React Component Example
```jsx
import React, { useState, useEffect } from 'react';

const UserTripsComponent = ({ userId }) => {
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (userId) {
            fetchUserTrips();
        }
    }, [userId]);

    const fetchUserTrips = async () => {
        setLoading(true);
        setError(null);
        
        try {
            const response = await fetch(
                `http://localhost:8084/api/v1/itinerary?userId=${encodeURIComponent(userId)}`
            );
            
            if (!response.ok) {
                throw new Error(`Failed to fetch trips: ${response.status}`);
            }
            
            const tripsData = await response.json();
            setTrips(tripsData);
        } catch (err) {
            setError(err.message);
            console.error('Error fetching user trips:', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div>Loading trips...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <div>
            <h2>Your Trips</h2>
            {trips.length === 0 ? (
                <p>No trips found. Create your first trip!</p>
            ) : (
                <ul>
                    {trips.map((trip) => (
                        <li key={trip.tripId}>
                            <h3>{trip.tripName}</h3>
                            <p>Destination: {trip.baseCity}</p>
                            <p>Duration: {trip.startDate} to {trip.endDate}</p>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default UserTripsComponent;
```

### Error Handling
```javascript
function handleGetTripsResponse(response, data) {
    if (response.ok && Array.isArray(data)) {
        // Success - data is array of trips
        return data;
    } else if (response.status === 400) {
        // Bad request - likely invalid userId
        throw new Error(data.message || 'Invalid user ID provided');
    } else if (response.status === 500) {
        // Server error
        throw new Error('Server error. Please try again later.');
    } else {
        // Other errors
        throw new Error(`Unexpected error: ${response.status}`);
    }
}
```

## Testing Instructions

1. **Import Collection**: Import the `GET_USER_TRIPS_POSTMAN_COLLECTION.json` file into Postman
2. **Set Environment**: Ensure base URL is set to `http://localhost:8084`
3. **Update User IDs**: Replace test user IDs with actual user IDs from your system
4. **Run Tests**: Execute the collection to validate all test cases
5. **Monitor Performance**: Check response times and ensure they meet requirements

## Validation Rules

### Request Validation
- `userId` parameter is required
- `userId` cannot be empty or null
- `userId` should be a valid string

### Response Validation
- Successful responses return HTTP 200 with array of trip summaries
- Error responses include proper status codes (400, 500)
- All responses include appropriate error messages when applicable

## Notes
- This endpoint does NOT return detailed daily plans (use GET /api/v1/itinerary/{tripId} for that)
- Empty array is returned for users with no trips (not an error)
- Response includes only trip summary information for performance
- All trips for the user are returned in a single response
