# Frontend Integration Guide - Pooling Service

This document provides integration details for frontend developers working with the Pooling Service endpoints.

## Base URL
```
http://localhost:8081/api/v1/groups
```

## Authentication
All endpoints require a `userId` parameter. This will be replaced with JWT authentication in the future.

## Endpoint Integration

### 1. Create Group
**Endpoint**: `POST /api/v1/groups?userId={userId}`

**Frontend Usage**:
```javascript
const createGroup = async (groupData, userId) => {
  try {
    const response = await fetch(`/api/v1/groups?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        groupName: groupData.name,
        tripId: groupData.existingTripId, // optional
        visibility: groupData.isPublic ? 'public' : 'private',
        preferences: {
          language: groupData.languages,
          interests: groupData.interests,
          budgetLevel: groupData.budget
        }
      })
    });
    
    const result = await response.json();
    
    if (response.ok) {
      // Display success message
      alert(result.message);
      // Redirect to group page
      window.location.href = `/groups/${result.groupId}`;
    } else {
      // Display error
      alert(result.message);
    }
  } catch (error) {
    console.error('Error creating group:', error);
    alert('Failed to create group');
  }
};

// Usage in React component
const handleCreateGroup = () => {
  const groupData = {
    name: groupNameInput.value,
    isPublic: isPublicCheckbox.checked,
    languages: ['English'],
    interests: selectedInterests,
    budget: budgetSelect.value
  };
  
  createGroup(groupData, currentUserId);
};
```

### 2. Invite User to Private Group
**Endpoint**: `POST /api/v1/groups/{groupId}/invite?userId={userId}`

**Frontend Usage**:
```javascript
const inviteUser = async (groupId, invitedUserId, creatorUserId) => {
  try {
    const response = await fetch(`/api/v1/groups/${groupId}/invite?userId=${creatorUserId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        invitedUserId: invitedUserId
      })
    });
    
    const result = await response.json();
    
    if (response.ok) {
      // Update UI to show invitation sent
      showNotification(result.message, 'success');
      // Refresh member list
      await loadGroupMembers(groupId);
    } else {
      showNotification(result.message, 'error');
    }
  } catch (error) {
    console.error('Error inviting user:', error);
    showNotification('Failed to invite user', 'error');
  }
};

// Usage in member management component
const handleInviteUser = () => {
  const email = inviteEmailInput.value;
  // Convert email to userId (or use user search)
  const invitedUserId = getUserIdFromEmail(email);
  inviteUser(currentGroupId, invitedUserId, currentUserId);
};
```

### 3. Add Place to Group Itinerary
**Endpoint**: `POST /api/v1/groups/{groupId}/itinerary/day/{day}/{type}?userId={userId}`

**Frontend Usage**:
```javascript
const addPlaceToItinerary = async (groupId, day, type, place, userId) => {
  try {
    const response = await fetch(`/api/v1/groups/${groupId}/itinerary/day/${day}/${type}?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: place.name,
        location: place.address,
        latitude: place.coordinates.lat,
        longitude: place.coordinates.lng,
        rating: place.rating,
        description: place.description,
        category: place.category,
        image: place.imageUrl,
        source: 'TripAdvisor' // or 'Google Places'
      })
    });
    
    const result = await response.json();
    
    if (response.ok) {
      // Update the itinerary view
      refreshItinerary(groupId);
      showNotification('Place added successfully', 'success');
    } else {
      showNotification(result.message, 'error');
    }
  } catch (error) {
    console.error('Error adding place:', error);
    showNotification('Failed to add place', 'error');
  }
};

// Usage in itinerary planning component with drag & drop
const handleDropPlace = (place, dayContainer) => {
  const day = parseInt(dayContainer.dataset.day);
  const type = dayContainer.dataset.type; // 'attractions', 'hotels', 'restaurants'
  
  addPlaceToItinerary(currentGroupId, day, type, place, currentUserId);
};
```

### 4. Update City for Day
**Endpoint**: `PATCH /api/v1/groups/{groupId}/itinerary/day/{day}/city?userId={userId}`

**Frontend Usage**:
```javascript
const updateCityForDay = async (groupId, day, city, userId) => {
  try {
    const response = await fetch(`/api/v1/groups/${groupId}/itinerary/day/${day}/city?userId=${userId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        city: city
      })
    });
    
    const result = await response.json();
    
    if (response.ok) {
      // Update the day's city display
      updateDayCityDisplay(day, city);
      showNotification('City updated successfully', 'success');
    } else {
      showNotification(result.message, 'error');
    }
  } catch (error) {
    console.error('Error updating city:', error);
    showNotification('Failed to update city', 'error');
  }
};

// Usage in city selection dropdown
const handleCityChange = (day, newCity) => {
  updateCityForDay(currentGroupId, day, newCity, currentUserId);
};
```

### 5. Get Group Details
**Endpoint**: `GET /api/v1/groups/{groupId}?userId={userId}`

**Frontend Usage**:
```javascript
const loadGroupDetails = async (groupId, userId) => {
  try {
    const response = await fetch(`/api/v1/groups/${groupId}?userId=${userId}`);
    const result = await response.json();
    
    if (response.ok) {
      // Update UI with group details
      updateGroupHeader(result);
      updateMembersList(result.userIds);
      updateGroupActions(result.actions);
      
      // Show join requests only for creators
      if (result.joinRequests) {
        updateJoinRequestsList(result.joinRequests);
      }
    } else {
      showNotification(result.message, 'error');
    }
  } catch (error) {
    console.error('Error loading group details:', error);
    showNotification('Failed to load group details', 'error');
  }
};

// Usage in group page component
useEffect(() => {
  loadGroupDetails(groupId, currentUserId);
}, [groupId, currentUserId]);
```

### 6. List Public Groups
**Endpoint**: `GET /api/v1/groups/public?userId={userId}&filters...`

**Frontend Usage**:
```javascript
const loadPublicGroups = async (userId, filters = {}) => {
  try {
    const params = new URLSearchParams({
      userId: userId,
      ...filters
    });
    
    const response = await fetch(`/api/v1/groups/public?${params}`);
    const groups = await response.json();
    
    if (response.ok) {
      // Render groups in suggestion-grid
      renderPublicGroups(groups);
    } else {
      showNotification('No public groups found', 'info');
    }
  } catch (error) {
    console.error('Error loading public groups:', error);
    showNotification('Failed to load public groups', 'error');
  }
};

// Usage in MyTripsPage.jsx
const renderPublicGroups = (groups) => {
  const groupsContainer = document.querySelector('.suggestions-grid');
  groupsContainer.innerHTML = groups.map(group => `
    <div class="suggestion-card">
      <h3>${group.groupName}</h3>
      <p>Destination: ${group.destination}</p>
      <p>Dates: ${group.startDate} to ${group.endDate}</p>
      <button class="suggestion-button" onclick="requestToJoinGroup('${group.groupId}')">
        Join Group
      </button>
    </div>
  `).join('');
};
```

### 7. Request to Join Public Group
**Endpoint**: `POST /api/v1/groups/{groupId}/join?userId={userId}`

**Frontend Usage**:
```javascript
const requestToJoinGroup = async (groupId, userProfile, userId) => {
  try {
    const response = await fetch(`/api/v1/groups/${groupId}/join?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        userProfile: {
          travelDates: {
            startDate: userProfile.startDate,
            endDate: userProfile.endDate
          },
          interests: userProfile.interests,
          language: userProfile.languages,
          budgetLevel: userProfile.budgetLevel,
          ageRange: userProfile.ageRange
        }
      })
    });
    
    const result = await response.json();
    
    if (response.ok) {
      showNotification(result.message, 'success');
      // Update button state
      updateJoinButtonState(groupId, 'pending');
    } else {
      showNotification(result.message, 'error');
    }
  } catch (error) {
    console.error('Error requesting to join group:', error);
    showNotification('Failed to send join request', 'error');
  }
};

// Usage with user profile form
const handleJoinGroupClick = (groupId) => {
  // Show join form modal
  showJoinGroupModal(groupId, (userProfile) => {
    requestToJoinGroup(groupId, userProfile, currentUserId);
  });
};
```

### 8. Approve/Reject Join Request
**Endpoint**: `PATCH /api/v1/groups/{groupId}/join/{joinerUserId}?creatorUserId={creatorUserId}`

**Frontend Usage**:
```javascript
const processJoinRequest = async (groupId, joinerUserId, decision, creatorUserId) => {
  try {
    const response = await fetch(`/api/v1/groups/${groupId}/join/${joinerUserId}?creatorUserId=${creatorUserId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        status: decision // 'approved' or 'rejected'
      })
    });
    
    const result = await response.json();
    
    if (response.ok) {
      // Remove from join requests list
      removeJoinRequestFromUI(joinerUserId);
      
      if (decision === 'approved') {
        // Add to members list
        addMemberToUI(joinerUserId);
      }
      
      showNotification(result.message, 'success');
    } else {
      showNotification(result.message, 'error');
    }
  } catch (error) {
    console.error('Error processing join request:', error);
    showNotification('Failed to process join request', 'error');
  }
};

// Usage in join requests management
const handleApprove = (joinerUserId) => {
  processJoinRequest(currentGroupId, joinerUserId, 'approved', currentUserId);
};

const handleReject = (joinerUserId) => {
  processJoinRequest(currentGroupId, joinerUserId, 'rejected', currentUserId);
};
```

### 9. Get Trip Suggestions
**Endpoint**: `POST /api/v1/groups/public/suggestions?userId={userId}`

**Frontend Usage**:
```javascript
const getTripSuggestions = async (preferences, userId) => {
  try {
    const response = await fetch(`/api/v1/groups/public/suggestions?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        travelDates: {
          startDate: preferences.startDate,
          endDate: preferences.endDate
        },
        interests: preferences.interests,
        language: preferences.languages,
        budgetLevel: preferences.budgetLevel,
        ageRange: preferences.ageRange
      })
    });
    
    const suggestions = await response.json();
    
    if (response.ok) {
      // Render suggestions with score badges
      renderTripSuggestions(suggestions);
    } else {
      showNotification('No matching trips found', 'info');
    }
  } catch (error) {
    console.error('Error getting trip suggestions:', error);
    showNotification('Failed to get suggestions', 'error');
  }
};

// Usage in SuggestionsModal.js
const renderTripSuggestions = (suggestions) => {
  const container = document.querySelector('.suggestions-grid');
  container.innerHTML = suggestions.map(suggestion => `
    <div class="suggestion-card">
      <div class="score-badge">${suggestion.score}% Match</div>
      <h3>${suggestion.groupName}</h3>
      <p>Destination: ${suggestion.destination}</p>
      <p>Dates: ${suggestion.startDate} to ${suggestion.endDate}</p>
      <button class="suggestion-button" onclick="requestToJoinGroup('${suggestion.groupId}')">
        Join Trip
      </button>
    </div>
  `).join('');
};
```

## Error Handling

All endpoints return consistent error responses:

```javascript
const handleApiError = (response, result) => {
  switch (response.status) {
    case 400:
      showNotification('Invalid request: ' + result.message, 'warning');
      break;
    case 403:
      showNotification('Access denied: ' + result.message, 'error');
      break;
    case 404:
      showNotification('Not found: ' + result.message, 'error');
      break;
    case 500:
      showNotification('Server error: ' + result.message, 'error');
      break;
    default:
      showNotification('Unexpected error: ' + result.message, 'error');
  }
};
```

## CSS Classes for Styling

Use these Tailwind CSS classes for consistent styling:

```css
.suggestions-grid {
  @apply grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6;
}

.suggestion-card {
  @apply bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow;
}

.suggestion-button {
  @apply bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded transition-colors;
}

.score-badge {
  @apply bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded-full;
}
```

## Real-time Updates

For real-time updates, consider implementing WebSocket connections or polling:

```javascript
// WebSocket connection for real-time group updates
const connectToGroupUpdates = (groupId) => {
  const ws = new WebSocket(`ws://localhost:8081/ws/groups/${groupId}`);
  
  ws.onmessage = (event) => {
    const update = JSON.parse(event.data);
    handleGroupUpdate(update);
  };
};

// Polling alternative
const pollForUpdates = (groupId) => {
  setInterval(async () => {
    await loadGroupDetails(groupId, currentUserId);
  }, 30000); // Poll every 30 seconds
};
```

This guide provides all the necessary JavaScript code for integrating with the Pooling Service endpoints in your React frontend application.
