# Trip Data Update Guide - Group Collaboration & Permissions

## 📋 Overview

This guide explains how trip data can be updated in the IslandHop system, including adding cities and places to trip itineraries. The system supports both individual and group trip management with proper access control mechanisms.

## 🔐 Permission System

### Individual Trips
- **Creator Only**: Only the trip creator (userId) can update the trip
- **Validation**: UserId is validated against trip ownership before any update

### Group Trips (Pooling Service Integration)
- **Any Group Member**: Any member of the associated group can update the trip itinerary
- **Validation Process**:
  1. Check if user is a member of the group linked to the trip
  2. Verify group exists and is active
  3. Allow updates if user has group membership

## 🎯 Available Update Endpoints

### 1. Update City for a Specific Day

**Endpoint**: `POST /api/v1/itinerary/{tripId}/day/{day}/city`

**Purpose**: Changes the city/destination for a specific day in the trip

**Request Format**:
```json
{
  "userId": "user_123",
  "city": "Kandy"
}
```

**Response Format**:
```json
{
  "status": "success",
  "tripId": "trip_456",
  "day": 2,
  "city": "Kandy",
  "message": "City updated successfully"
}
```

**JavaScript Implementation**:
```javascript
const updateCityForDay = async (tripId, day, userId, city) => {
  try {
    const response = await fetch(`http://localhost:8084/api/v1/itinerary/${tripId}/day/${day}/city`, {
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
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Error updating city:', error);
    throw error;
  }
};
```

### 2. Add Place to Trip Itinerary

**Endpoint**: `POST /api/v1/itinerary/{tripId}/day/{day}/{type}?userId={userId}`

**Purpose**: Adds a place (attraction, hotel, or restaurant) to a specific day

**Path Parameters**:
- `tripId`: UUID of the trip
- `day`: Day number (1-based indexing)
- `type`: Type of place (`attractions`, `hotels`, `restaurants`)
- `userId`: User ID as query parameter

**Request Format**:
```json
{
  "name": "Gangaramaya Temple",
  "location": "Colombo",
  "address": "61 Sri Jinaratana Rd, Colombo 00200",
  "category": "Cultural",
  "latitude": 6.9034,
  "longitude": 79.8690,
  "rating": 4.5,
  "description": "Historic Buddhist temple in Colombo",
  "duration": "2-3 hours",
  "price": "$5-15",
  "openHours": "8:00 AM - 6:00 PM",
  "source": "TripAdvisor",
  "imageUrl": "https://example.com/image.jpg"
}
```

**Response Format**:
```json
{
  "status": "success",
  "tripId": "trip_456",
  "day": 2,
  "type": "attractions",
  "place": {
    "name": "Gangaramaya Temple",
    "userSelected": true,
    "addedAt": "2025-07-18T12:00:00Z"
  },
  "message": "Place added successfully to attractions for day 2"
}
```

**JavaScript Implementation**:
```javascript
const addPlaceToTrip = async (tripId, day, type, place, userId) => {
  try {
    const response = await fetch(
      `http://localhost:8084/api/v1/itinerary/${tripId}/day/${day}/${type}?userId=${userId}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(place)
      }
    );

    const result = await response.json();
    
    if (response.ok) {
      console.log('Place added successfully:', result);
      return result;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Error adding place:', error);
    throw error;
  }
};
```

## 🏠 Group Trip Updates (Pooling Service)

For group trips created through the pooling service, the system provides additional endpoints that handle group permissions automatically.

### Group Itinerary Update

**Endpoint**: `POST /api/v1/groups/{groupId}/itinerary/day/{day}/{type}?userId={userId}`

**Purpose**: Add place to group trip with automatic group membership validation

**JavaScript Implementation**:
```javascript
const addPlaceToGroupTrip = async (groupId, day, type, place, userId) => {
  try {
    const response = await fetch(
      `http://localhost:8086/api/v1/groups/${groupId}/itinerary/day/${day}/${type}?userId=${userId}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(place)
      }
    );

    const result = await response.json();
    
    if (response.ok) {
      console.log('Place added to group trip:', result);
      // Update UI to show the new place
      updateTripPlanDisplay(groupId, day, type, place);
      return result;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Error adding place to group trip:', error);
    throw error;
  }
};
```

### Group City Update

**Endpoint**: `PATCH /api/v1/groups/{groupId}/itinerary/day/{day}/city?userId={userId}`

**Purpose**: Update city for a day in group trip

**JavaScript Implementation**:
```javascript
const updateGroupTripCity = async (groupId, day, city, userId) => {
  try {
    const response = await fetch(
      `http://localhost:8086/api/v1/groups/${groupId}/itinerary/day/${day}/city?userId=${userId}`,
      {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ city })
      }
    );

    const result = await response.json();
    
    if (response.ok) {
      console.log('City updated for group trip:', result);
      // Update UI
      updateDayCityDisplay(day, city);
      return result;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Error updating city:', error);
    throw error;
  }
};
```

## 🔍 Permission Validation Process

### Backend Validation Flow

1. **Extract User ID**: From request body or query parameter
2. **Find Trip**: Locate trip by tripId
3. **Check Ownership/Membership**:
   - For individual trips: `trip.userId === requestUserId`
   - For group trips: Check if user is member of `trip.groupId`
4. **Validate Day**: Ensure day number is within trip duration
5. **Apply Updates**: If all validations pass, update the trip data

### Group Membership Check (Backend)

```java
// Example validation logic
public boolean canUserUpdateTrip(String tripId, String userId) {
    TripPlan trip = tripRepository.findById(tripId);
    
    if (trip.getType().equals("individual")) {
        return trip.getUserId().equals(userId);
    } else if (trip.getType().equals("group")) {
        Group group = groupRepository.findById(trip.getGroupId());
        return group.isMember(userId);
    }
    
    return false;
}
```

## ⚠️ Error Handling

### Common Error Scenarios

1. **Unauthorized Access (403)**:
   ```json
   {
     "status": "error",
     "message": "You are not authorized to modify this trip"
   }
   ```

2. **Trip Not Found (404)**:
   ```json
   {
     "status": "error",
     "message": "Trip not found with ID: trip_123"
   }
   ```

3. **Invalid Day (400)**:
   ```json
   {
     "status": "error",
     "message": "Day 10 is invalid. Trip has only 5 days"
   }
   ```

4. **Validation Error (400)**:
   ```json
   {
     "status": "error",
     "message": "Validation failed",
     "errors": {
       "city": "City cannot be blank"
     }
   }
   ```

### Frontend Error Handling

```javascript
const handleTripUpdate = async (updateFunction) => {
  try {
    const result = await updateFunction();
    showSuccessMessage('Trip updated successfully!');
    return result;
  } catch (error) {
    if (error.message.includes('not authorized')) {
      showErrorMessage('You do not have permission to update this trip');
    } else if (error.message.includes('not found')) {
      showErrorMessage('Trip not found. It may have been deleted.');
    } else {
      showErrorMessage(`Update failed: ${error.message}`);
    }
    throw error;
  }
};
```

## 🚀 Complete Integration Example

### Group Trip Collaboration Workflow

```javascript
class TripCollaborationManager {
  constructor(groupId, userId) {
    this.groupId = groupId;
    this.userId = userId;
    this.baseUrl = 'http://localhost:8086/api/v1/groups';
  }

  // Add attraction to trip
  async addAttraction(day, attraction) {
    return this.addPlace(day, 'attractions', attraction);
  }

  // Add hotel to trip
  async addHotel(day, hotel) {
    return this.addPlace(day, 'hotels', hotel);
  }

  // Add restaurant to trip
  async addRestaurant(day, restaurant) {
    return this.addPlace(day, 'restaurants', restaurant);
  }

  // Generic place addition
  async addPlace(day, type, place) {
    try {
      const response = await fetch(
        `${this.baseUrl}/${this.groupId}/itinerary/day/${day}/${type}?userId=${this.userId}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(place)
        }
      );

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message);
      }

      const result = await response.json();
      
      // Notify other group members (via WebSocket or polling)
      this.notifyGroupMembers('place_added', {
        day,
        type,
        place: place.name,
        addedBy: this.userId
      });

      return result;
    } catch (error) {
      console.error(`Error adding ${type} to day ${day}:`, error);
      throw error;
    }
  }

  // Update city for a day
  async updateCity(day, city) {
    try {
      const response = await fetch(
        `${this.baseUrl}/${this.groupId}/itinerary/day/${day}/city?userId=${this.userId}`,
        {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ city })
        }
      );

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message);
      }

      const result = await response.json();
      
      // Notify group members
      this.notifyGroupMembers('city_updated', {
        day,
        city,
        updatedBy: this.userId
      });

      return result;
    } catch (error) {
      console.error(`Error updating city for day ${day}:`, error);
      throw error;
    }
  }

  // Real-time notification system
  notifyGroupMembers(action, data) {
    // Implementation depends on your real-time system
    // Could use WebSockets, Server-Sent Events, or polling
    console.log(`Notifying group members: ${action}`, data);
    
    // Example: WebSocket notification
    if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
      this.websocket.send(JSON.stringify({
        type: 'trip_update',
        groupId: this.groupId,
        action,
        data,
        timestamp: new Date().toISOString()
      }));
    }
  }
}

// Usage example
const tripManager = new TripCollaborationManager('group_123', 'user_456');

// Add an attraction to day 2
await tripManager.addAttraction(2, {
  name: 'Sigiriya Rock Fortress',
  location: 'Sigiriya',
  category: 'Historical',
  rating: 4.8,
  duration: '3-4 hours'
});

// Update city for day 3
await tripManager.updateCity(3, 'Kandy');
```

## 📱 Frontend Integration Tips

### 1. Real-time Updates
Implement real-time synchronization so all group members see updates immediately:

```javascript
// WebSocket connection for real-time updates
const connectToTripUpdates = (groupId) => {
  const ws = new WebSocket(`ws://localhost:8081/ws/groups/${groupId}/trip`);
  
  ws.onmessage = (event) => {
    const update = JSON.parse(event.data);
    handleTripUpdate(update);
  };
};

const handleTripUpdate = (update) => {
  switch (update.action) {
    case 'place_added':
      addPlaceToUI(update.data.day, update.data.type, update.data.place);
      showNotification(`${update.data.addedBy} added ${update.data.place} to day ${update.data.day}`);
      break;
    case 'city_updated':
      updateCityInUI(update.data.day, update.data.city);
      showNotification(`${update.data.updatedBy} changed day ${update.data.day} to ${update.data.city}`);
      break;
  }
};
```

### 2. Optimistic Updates
Update UI immediately and rollback on error:

```javascript
const optimisticCityUpdate = async (day, newCity) => {
  const oldCity = getCurrentCity(day);
  
  // Update UI immediately
  updateCityInUI(day, newCity);
  
  try {
    await tripManager.updateCity(day, newCity);
    // Success - no action needed
  } catch (error) {
    // Rollback UI change
    updateCityInUI(day, oldCity);
    showErrorMessage('Failed to update city: ' + error.message);
  }
};
```

### 3. Conflict Resolution
Handle cases where multiple users edit simultaneously:

```javascript
const handleConflictResolution = (update) => {
  if (update.conflict) {
    showConflictDialog({
      message: `${update.conflictBy} also modified this. Choose version:`,
      options: [
        { label: 'Keep my changes', action: 'keep_local' },
        { label: 'Accept their changes', action: 'accept_remote' },
        { label: 'Merge both', action: 'merge' }
      ]
    });
  }
};
```

## 🎯 Summary

The IslandHop trip update system provides:

1. **Flexible Permissions**: Individual trip owners and group members can update trips
2. **Multiple Update Types**: City changes and place additions
3. **Proper Validation**: All updates validate user permissions and data integrity
4. **Real-time Collaboration**: Group members can collaborate on trip planning
5. **Error Handling**: Comprehensive error responses for all scenarios

This system ensures that trip planning is collaborative for groups while maintaining security and data consistency.
