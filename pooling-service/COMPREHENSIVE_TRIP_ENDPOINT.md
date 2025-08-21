# Pooling Service Frontend Integration Guide

## Comprehensive Trip Details Endpoint

### GET /api/v1/public-pooling/trips/{tripId}/comprehensive?userId={userId}

**Description:** Get complete trip details including itinerary and joined group members in a single API call. This endpoint is publicly accessible for both logged-in and anonymous users.

**Parameters:**
- `tripId` (path): The ID of the trip
- `userId` (query, optional): The ID of the user making the request (for personalization)

**Response Structure:**
```json
{
  "tripDetails": {
    "tripId": "trip_001",
    "tripName": "Sri Lanka Adventure",
    "startDate": "2025-08-10",
    "endDate": "2025-08-15",
    "baseCity": "Colombo",
    "budgetLevel": "Medium",
    "activityPacing": "Normal",
    "preferredActivities": ["Hiking", "Cultural Tours"],
    "preferredTerrains": ["Beach", "Mountain"],
    "multiCityAllowed": true,
    "dailyPlans": [
      {
        "day": 1,
        "city": "Colombo",
        "userSelected": true,
        "attractionsCount": 3,
        "hotelsCount": 2,
        "restaurantsCount": 4,
        "attractions": [
          {
            "name": "Gangaramaya Temple",
            "category": "Cultural",
            "rating": 4.5,
            "address": "Sri Jinaratana Road, Colombo 02",
            "userSelected": true
          }
        ],
        "hotels": [...],
        "restaurants": [...]
      }
    ],
    "createdAt": "2025-07-20T10:00:00Z",
    "lastUpdated": "2025-07-22T14:30:00Z"
  },
  "groupInfo": {
    "groupId": "group_123",
    "groupName": "Adventure Seekers",
    "visibility": "public",
    "status": "active",
    "groupLeader": "user_123",
    "currentMembers": 3,
    "maxMembers": 6,
    "availableSlots": 3,
    "requiresApproval": true,
    "createdAt": "2025-07-20T10:00:00Z",
    "lastUpdated": "2025-07-22T14:30:00Z"
  },
  "members": [
    {
      "userId": "user_123",
      "name": "User user_123",
      "email": "user_123@example.com",
      "role": "leader",
      "joinedAt": "2025-07-20T10:00:00Z",
      "status": "active",
      "preferences": {
        "budgetLevel": "Medium",
        "preferredActivities": ["Hiking", "Cultural Tours"],
        "preferredTerrains": ["Mountain", "Historical"],
        "activityPacing": "Normal"
      }
    }
  ],
  "status": "success",
  "message": "Comprehensive trip details retrieved successfully",
  "fetchedAt": "2025-07-22T15:00:00Z"
}
```

## JavaScript Integration

### Fetch Comprehensive Trip Details

```javascript
/**
 * Fetch comprehensive trip details including itinerary and joined members
 * @param {string} tripId - The trip ID
 * @param {string} userId - The requesting user ID (optional, for personalization)
 * @returns {Promise<Object>} Comprehensive trip response
 */
async function getComprehensiveTripDetails(tripId, userId = null) {
    try {
        const url = userId 
            ? `/api/v1/public-pooling/trips/${tripId}/comprehensive?userId=${userId}`
            : `/api/v1/public-pooling/trips/${tripId}/comprehensive`;
            
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error fetching comprehensive trip details:', error);
        throw error;
    }
}

// Usage examples
// For logged-in users
getComprehensiveTripDetails('trip_001', 'user_123')
    .then(tripData => {
        console.log('Trip Details:', tripData.tripDetails);
        console.log('Group Info:', tripData.groupInfo);
        console.log('Members:', tripData.members);
    })
    .catch(error => {
        console.error('Failed to load trip details:', error);
    });

// For anonymous users
getComprehensiveTripDetails('trip_001')
    .then(tripData => {
        console.log('Public Trip Details:', tripData.tripDetails);
        console.log('Group Members:', tripData.members);
    })
    .catch(error => {
        console.error('Failed to load trip details:', error);
    });
```

### React Component Example

```jsx
import React, { useState, useEffect } from 'react';

const ComprehensiveTripView = ({ tripId, userId = null }) => {
    const [tripData, setTripData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchTripData = async () => {
            try {
                setLoading(true);
                const data = await getComprehensiveTripDetails(tripId, userId);
                setTripData(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        if (tripId) {
            fetchTripData();
        }
    }, [tripId, userId]);

    if (loading) return <div>Loading trip details...</div>;
    if (error) return <div>Error: {error}</div>;
    if (!tripData) return <div>No trip data available</div>;

    const { tripDetails, groupInfo, members } = tripData;

    return (
        <div className="comprehensive-trip-view">
            {/* Trip Information */}
            <section className="trip-info">
                <h1>{tripDetails.tripName}</h1>
                <p>{tripDetails.startDate} to {tripDetails.endDate}</p>
                <p>Base City: {tripDetails.baseCity}</p>
                <p>Budget Level: {tripDetails.budgetLevel}</p>
            </section>

            {/* Group Information */}
            {groupInfo && (
                <section className="group-info">
                    <h2>Group: {groupInfo.groupName}</h2>
                    <p>Members: {groupInfo.currentMembers}/{groupInfo.maxMembers}</p>
                    <p>Available Slots: {groupInfo.availableSlots}</p>
                    <p>Status: {groupInfo.status}</p>
                </section>
            )}

            {/* Daily Plans */}
            <section className="daily-plans">
                <h2>Itinerary</h2>
                {tripDetails.dailyPlans.map(day => (
                    <div key={day.day} className="daily-plan">
                        <h3>Day {day.day}: {day.city}</h3>
                        <p>Attractions: {day.attractionsCount}</p>
                        <p>Hotels: {day.hotelsCount}</p>
                        <p>Restaurants: {day.restaurantsCount}</p>
                        
                        {day.attractions.length > 0 && (
                            <div className="attractions">
                                <h4>Attractions</h4>
                                {day.attractions.map((attraction, idx) => (
                                    <div key={idx} className="place-item">
                                        <span>{attraction.name}</span>
                                        <span>Rating: {attraction.rating}</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
            </section>

            {/* Members */}
            {members && members.length > 0 && (
                <section className="members">
                    <h2>Group Members</h2>
                    {members.map(member => (
                        <div key={member.userId} className="member-item">
                            <span>{member.name}</span>
                            <span>Role: {member.role}</span>
                            <span>Status: {member.status}</span>
                        </div>
                    ))}
                </section>
            )}
        </div>
    );
};

export default ComprehensiveTripView;
```

## Error Handling

```javascript
function handleApiError(error, response) {
    if (response.status === 404) {
        throw new Error('Trip not found');
    } else if (response.status === 500) {
        throw new Error('Server error. Please try again later.');
    } else {
        throw new Error(`Unexpected error: ${response.status}`);
    }
}
```

## Benefits of This Public Comprehensive Endpoint

1. **Public Access**: Accessible to both logged-in and anonymous users
2. **Single API Call**: Get all trip-related data in one request
3. **Complete Information**: Trip itinerary and joined group members
4. **Efficient**: Reduces network overhead and improves performance
5. **Consistent Data**: All information is fetched at the same time, ensuring consistency
6. **Privacy-Focused**: Only shows public information, no sensitive data like invitations or requests
7. **SEO Friendly**: Can be used for public trip pages and sharing

## Testing the Endpoint

You can test this endpoint with:

```bash
# For anonymous users
curl -X GET "http://localhost:8080/api/v1/public-pooling/trips/trip_001/comprehensive" \
  -H "Content-Type: application/json"

# For logged-in users (with personalization)
curl -X GET "http://localhost:8080/api/v1/public-pooling/trips/trip_001/comprehensive?userId=user_123" \
  -H "Content-Type: application/json"
```

## Other Existing Endpoints

### Pre-check Compatible Groups
- **POST** `/api/v1/public-pooling/pre-check`
- **GET** `/api/v1/public-pooling/groups/compatible/{tripId}`

### Group Management
- **POST** `/api/v1/public-pooling/groups`
- **POST** `/api/v1/public-pooling/groups/{groupId}/save-trip`
- **POST** `/api/v1/public-pooling/groups/{groupId}/finalize`

### Health Check
- **GET** `/api/v1/public-pooling/health`
