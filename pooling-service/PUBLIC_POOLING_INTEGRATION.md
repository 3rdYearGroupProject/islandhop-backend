# Public Pooling Service - Frontend Integration Guide

## Overview

The Public Pooling Service extends the existing group functionality to support public pooling groups with trip comparison and suggestions. This service helps users find compatible travel groups and join existing ones instead of creating redundant trips.

## New Endpoints

### 1. POST /v1/groups/with-trip - Create Public Pooling Group

**Purpose:** Creates a new public pooling group with trip planning simultaneously.

**Request Body:**
```json
{
  "userId": "user_123",
  "groupName": "Sri Lanka Adventure Seekers",
  "tripName": "Cultural Sri Lanka Tour",
  "startDate": "2025-08-15",
  "endDate": "2025-08-22",
  "baseCity": "Colombo",
  "arrivalTime": "14:30",
  "multiCityAllowed": true,
  "activityPacing": "Normal",
  "budgetLevel": "Medium",
  "preferredTerrains": ["Beach", "Mountain", "Historical"],
  "preferredActivities": ["Cultural Tours", "Hiking", "Photography"],
  "visibility": "public",
  "maxMembers": 6,
  "requiresApproval": false,
  "additionalPreferences": {}
}
```

**Response:**
```json
{
  "status": "success",
  "groupId": "group_abc123",
  "tripId": "trip_def456",
  "message": "Group with trip created successfully",
  "isDraft": true
}
```

### 2. GET /v1/groups/{groupId}/trip-suggestions - Get Trip Suggestions

**Purpose:** Gets compatible groups for a draft group based on trip similarity.

**Query Parameters:**
- `userId`: String (required) - The requesting user's ID

**Response:**
```json
{
  "status": "success",
  "groupId": "group_abc123",
  "tripId": "trip_def456",
  "suggestions": [
    {
      "groupId": "group_xyz789",
      "groupName": "Lanka Explorers",
      "tripId": "trip_uvw012",
      "tripName": "Sri Lanka Heritage Tour",
      "compatibilityScore": 0.85,
      "currentMembers": 3,
      "maxMembers": 8,
      "commonDestinations": ["Kandy", "Galle", "Sigiriya"],
      "commonPreferences": ["Cultural Tours", "Photography"],
      "createdBy": "user_456",
      "startDate": "2025-08-16",
      "endDate": "2025-08-23",
      "baseCity": "Colombo"
    }
  ],
  "message": "Found 1 compatible groups."
}
```

### 3. POST /v1/groups/{groupId}/finalize-trip - Finalize Trip

**Purpose:** Finalizes a trip or joins an existing group based on user choice.

**Request Body (Finalize):**
```json
{
  "userId": "user_123",
  "action": "finalize"
}
```

**Request Body (Join):**
```json
{
  "userId": "user_123",
  "action": "join",
  "targetGroupId": "group_xyz789"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Trip finalized successfully",
  "groupId": "group_abc123",
  "tripId": "trip_def456",
  "action": "finalized",
  "success": true
}
```

## JavaScript Integration

### Creating a Public Pooling Group

```javascript
const createPublicPoolingGroup = async (groupData) => {
    try {
        const response = await fetch('http://localhost:8086/v1/groups/with-trip', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(groupData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            console.log('Public pooling group created:', result);
            return result;
        } else {
            throw new Error(result.message || 'Failed to create group');
        }
    } catch (error) {
        console.error('Error creating public pooling group:', error);
        throw error;
    }
};

// Usage
const groupData = {
    userId: "user_123",
    groupName: "Sri Lanka Adventure Seekers",
    tripName: "Cultural Sri Lanka Tour",
    startDate: "2025-08-15",
    endDate: "2025-08-22",
    baseCity: "Colombo",
    visibility: "public",
    maxMembers: 6,
    preferredTerrains: ["Beach", "Mountain"],
    preferredActivities: ["Cultural Tours", "Hiking"]
};

createPublicPoolingGroup(groupData)
    .then(result => {
        // Store groupId and tripId for later use
        localStorage.setItem('draftGroupId', result.groupId);
        localStorage.setItem('draftTripId', result.tripId);
        
        // Proceed to trip planning phase
        redirectToTripPlanning(result.tripId);
    })
    .catch(error => {
        // Handle error
        showErrorMessage(error.message);
    });
```

### Getting Trip Suggestions

```javascript
const getTripSuggestions = async (groupId, userId) => {
    try {
        const response = await fetch(
            `http://localhost:8086/v1/groups/${groupId}/trip-suggestions?userId=${userId}`,
            {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            }
        );
        
        const result = await response.json();
        
        if (response.ok) {
            return result;
        } else {
            throw new Error(result.message || 'Failed to get suggestions');
        }
    } catch (error) {
        console.error('Error getting trip suggestions:', error);
        throw error;
    }
};

// Usage
const groupId = localStorage.getItem('draftGroupId');
const userId = getCurrentUserId();

getTripSuggestions(groupId, userId)
    .then(suggestions => {
        if (suggestions.suggestions && suggestions.suggestions.length > 0) {
            // Show suggestions to user
            displaySuggestions(suggestions.suggestions);
        } else {
            // No suggestions found, proceed to finalization
            finalizeTripAutomatically(groupId, userId);
        }
    })
    .catch(error => {
        // Handle error
        showErrorMessage(error.message);
    });
```

### Finalizing Trip or Joining Group

```javascript
const finalizeTrip = async (groupId, userId, action, targetGroupId = null) => {
    try {
        const requestBody = {
            userId: userId,
            action: action
        };
        
        if (targetGroupId) {
            requestBody.targetGroupId = targetGroupId;
        }
        
        const response = await fetch(
            `http://localhost:8086/v1/groups/${groupId}/finalize-trip`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody)
            }
        );
        
        const result = await response.json();
        
        if (response.ok) {
            return result;
        } else {
            throw new Error(result.message || 'Failed to finalize trip');
        }
    } catch (error) {
        console.error('Error finalizing trip:', error);
        throw error;
    }
};

// Usage - Finalize current trip
finalizeTrip(groupId, userId, 'finalize')
    .then(result => {
        console.log('Trip finalized successfully');
        // Redirect to active group page
        redirectToGroupPage(result.groupId);
    })
    .catch(error => {
        showErrorMessage(error.message);
    });

// Usage - Join existing group
finalizeTrip(groupId, userId, 'join', selectedGroupId)
    .then(result => {
        console.log('Successfully joined existing group');
        // Redirect to joined group page
        redirectToGroupPage(result.groupId);
    })
    .catch(error => {
        showErrorMessage(error.message);
    });
```

## React Component Example

```jsx
import React, { useState, useEffect } from 'react';

const PublicPoolingFlow = ({ userId }) => {
    const [draftGroupId, setDraftGroupId] = useState(null);
    const [suggestions, setSuggestions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [phase, setPhase] = useState('create'); // create -> plan -> suggest -> finalize

    const createGroup = async (groupData) => {
        setLoading(true);
        try {
            const result = await createPublicPoolingGroup(groupData);
            setDraftGroupId(result.groupId);
            setPhase('plan');
        } catch (error) {
            console.error('Error creating group:', error);
        } finally {
            setLoading(false);
        }
    };

    const getSuggestions = async () => {
        setLoading(true);
        try {
            const result = await getTripSuggestions(draftGroupId, userId);
            setSuggestions(result.suggestions || []);
            setPhase('suggest');
        } catch (error) {
            console.error('Error getting suggestions:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleUserChoice = async (action, targetGroupId = null) => {
        setLoading(true);
        try {
            const result = await finalizeTrip(draftGroupId, userId, action, targetGroupId);
            // Handle successful finalization
            if (action === 'finalize') {
                console.log('Trip finalized');
            } else {
                console.log('Joined existing group');
            }
            setPhase('complete');
        } catch (error) {
            console.error('Error finalizing trip:', error);
        } finally {
            setLoading(false);
        }
    };

    const renderSuggestions = () => (
        <div className="suggestions">
            <h3>Found {suggestions.length} compatible groups!</h3>
            <p>You can join an existing group or proceed with your own trip.</p>
            
            {suggestions.map(group => (
                <div key={group.groupId} className="suggestion-card">
                    <h4>{group.groupName}</h4>
                    <p>Trip: {group.tripName}</p>
                    <p>Compatibility: {(group.compatibilityScore * 100).toFixed(1)}%</p>
                    <p>Members: {group.currentMembers}/{group.maxMembers}</p>
                    <p>Dates: {group.startDate} to {group.endDate}</p>
                    <p>Base City: {group.baseCity}</p>
                    <button 
                        onClick={() => handleUserChoice('join', group.groupId)}
                        disabled={loading}
                    >
                        Join This Group
                    </button>
                </div>
            ))}
            
            <div className="finalize-option">
                <h4>Or proceed with your own trip</h4>
                <button 
                    onClick={() => handleUserChoice('finalize')}
                    disabled={loading}
                >
                    Continue With My Trip
                </button>
            </div>
        </div>
    );

    return (
        <div className="public-pooling-flow">
            {phase === 'create' && (
                <GroupCreationForm onSubmit={createGroup} loading={loading} />
            )}
            
            {phase === 'plan' && (
                <TripPlanningComponent 
                    groupId={draftGroupId} 
                    onComplete={getSuggestions}
                />
            )}
            
            {phase === 'suggest' && renderSuggestions()}
            
            {phase === 'complete' && (
                <div className="completion-message">
                    <h3>All set! Your trip is ready.</h3>
                </div>
            )}
        </div>
    );
};

export default PublicPoolingFlow;
```

## Workflow Summary

1. **Group Creation**: User creates a public pooling group with trip details
2. **Trip Planning**: User plans their trip using the trip-planning service
3. **Suggestions**: System finds compatible groups and presents options
4. **User Choice**: User either joins an existing group or finalizes their own trip
5. **Completion**: User is redirected to their active group

This implementation provides a seamless experience for users to discover and join compatible travel groups while maintaining the flexibility to create their own trips.
