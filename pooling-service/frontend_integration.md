# Frontend Integration Guide - Enhanced Pooling Service with Trip Planning Integration

This document provides comprehensive integration details for frontend developers working with the Enhanced Pooling Service, including integration with the Trip Planning Service for detailed trip information and place management.

## Base URLs
```
Pooling Service: http://localhost:8086/api/v1/groups
Public Pooling: http://localhost:8086/api/v1/public-pooling
Trip Planning Service: http://localhost:8082/api/v1/itinerary (via pooling service clients)
User Service: http://localhost:8083/api/v1/tourist (via pooling service clients)
```

## Authentication
All endpoints require a `userId` parameter and optionally `userEmail` for enhanced features. This will be replaced with JWT authentication in the future.

## 🚀 Enhanced Public Groups Display

### Get Enhanced Public Groups (NEW)
**Endpoint**: `GET /api/v1/groups/public/enhanced`

**Purpose**: Get detailed public groups with trip information, creator names, cities, and attractions fetched from the Trip Planning Service

**Request Parameters**:
```
userId: user_123 (required)
userEmail: user@example.com (optional, for creator name lookup via User Service)
baseCity: Colombo (optional)
startDate: 2025-07-20 (optional)
endDate: 2025-07-25 (optional)
budgetLevel: Medium (optional)
preferredActivities: ["Hiking", "Cultural Tours"] (optional)
```

**JavaScript Example**:
```javascript
async function getEnhancedPublicGroups(filters) {
    // Get user data from frontend storage (Firebase user data)
    const user = getCurrentUser(); // Your method to get current user
    
    const params = new URLSearchParams({
        userId: user.uid, // Firebase user ID
        userEmail: user.email // Firebase user email for name resolution
    });
    
    // Add optional filters
    if (filters.baseCity) params.append('baseCity', filters.baseCity);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.budgetLevel) params.append('budgetLevel', filters.budgetLevel);
    if (filters.preferredActivities) {
        filters.preferredActivities.forEach(activity => 
            params.append('preferredActivities', activity)
        );
    }
        );
    }
    
    const response = await fetch(`/api/v1/groups/public/enhanced?${params}`);
    return await response.json();
}
```

**Response JSON Example**:
```json
[
  {
    "groupId": "group_008",
    "tripId": "trip_456",
    "tripName": "Adventure to Ella",
    "groupName": "Adventure to Ella",
    "creatorUserId": "tEJNF1Lo1bUxpzwoPdIvRQwa9hm1",
    "creatorName": "John Doe",
    "baseCity": "Kandy",
    "cities": ["Kandy", "Nuwara Eliya", "Ella"],
    "startDate": "2025-08-15",
    "endDate": "2025-08-17",
    "formattedDateRange": "Aug 15-17, 2025",
    "tripDurationDays": 3,
    "memberCount": 3,
    "maxMembers": 5,
    "memberCountText": "3 participants / 5",
    "topAttractions": [
      "Tea Plantations",
      "Nine Arch Bridge", 
      "Little Adams Peak"
    ],
    "budgetLevel": "Medium",
    "activityPacing": "Normal",
    "preferredActivities": ["Hiking", "Cultural Tours"],
    "preferredTerrains": ["Mountain", "Nature"],
    "status": "active",
    "compatibilityScore": 0.85
  }
]
```

## 🚀 Enhanced Hybrid Trip Suggestion Workflow

The enhanced system supports:
1. **Pre-Check**: Check for compatible groups before creating a new one
2. **Unified Group Creation**: Single endpoint for both private and public groups using `visibility` parameter
3. **Post-Creation Suggestions**: Get suggestions during trip finalization (for public groups)
4. **Advanced Filtering**: Filter public groups by preferences and compatibility
5. **Smart Joining**: Join existing groups and discard drafts automatically

## 📋 Unified Group Creation Endpoint

### Create Group with Trip (Unified for Public & Private)
**Endpoint**: `POST /api/v1/groups/with-trip`

**Purpose**: Creates both private and public groups with trip planning. The `visibility` parameter determines the behavior:
- `"private"`: Group is immediately finalized
- `"public"`: Group starts as draft for suggestion workflow

**Request JSON Example**:
```json
{
  "userId": "tEJNF1Lo1bUxpzwoPdIvRQwa9hm1",
  "userEmail": "john.doe@example.com",
  "groupName": "Adventure Seekers",
  "tripName": "Sri Lanka Adventure",
  "startDate": "2025-08-15",
  "endDate": "2025-08-22",
  "baseCity": "Colombo",
  "arrivalTime": "14:30",
  "multiCityAllowed": true,
  "activityPacing": "Normal",
  "budgetLevel": "Medium",
  "preferredTerrains": ["Mountain", "Beach"],
  "preferredActivities": ["Hiking", "Cultural Tours"],
  "visibility": "public",
  "maxMembers": 6,
  "requiresApproval": false
}
```

**Frontend Implementation**:
```javascript
const createGroupWithTrip = async (groupData) => {
    try {
        // Get current user data from frontend storage (Firebase user data)
        const currentUser = getCurrentUser(); // Your method to get current user
        
        const response = await fetch('/api/v1/groups/with-trip', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: currentUser.uid, // Firebase user ID
                userEmail: currentUser.email, // Firebase user email for creator name lookup
                groupName: groupData.groupName,
                tripName: groupData.tripName,
                startDate: groupData.startDate,
                endDate: groupData.endDate,
                baseCity: groupData.baseCity,
                arrivalTime: groupData.arrivalTime || "",
                multiCityAllowed: groupData.multiCityAllowed ?? true,
                activityPacing: groupData.activityPacing || "Normal",
                budgetLevel: groupData.budgetLevel || "Medium",
                preferredTerrains: groupData.preferredTerrains || [],
                preferredActivities: groupData.preferredActivities || [],
                visibility: groupData.visibility || "public", // "public" or "private"
                maxMembers: groupData.maxMembers || 6,
                requiresApproval: groupData.requiresApproval || false,
                additionalPreferences: groupData.additionalPreferences || {}
            })
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            if (groupData.visibility === 'public') {
                // Public group - handle suggestion workflow if needed
                handlePublicGroupCreated(result);
            } else {
                // Private group - immediately finalized
                handlePrivateGroupCreated(result);
            }
        } else {
            throw new Error(result.message || 'Failed to create group');
        }
        
        return result;
        
    } catch (error) {
        console.error('Error creating group:', error);
        throw error;
    }
};

// Handle public group creation (draft state)
function handlePublicGroupCreated(result) {
    console.log('Public group created in draft mode:', result.groupId);
    // Show trip planning interface
    // After trip planning, finalize or get suggestions
}

// Handle private group creation (immediately finalized)
function handlePrivateGroupCreated(result) {
    console.log('Private group created and finalized:', result.groupId);
    // Navigate to group management or trip planning
}
```

## 📋 New Hybrid Endpoints

> **Note**: The unified `/api/v1/groups/with-trip` endpoint now handles both public and private group creation. The dedicated public pooling endpoints (`/api/v1/public-pooling/groups`) are still available but recommended to use the unified approach for consistency.

### 1. Pre-Check Compatible Groups (NEW)
**Endpoint**: `POST /api/v1/public-pooling/pre-check`

**Purpose**: Check for existing compatible public groups before creating a new one

**Request JSON Example**:
```json
{
  "userId": "user_123",
  "baseCity": "Colombo",
  "startDate": "2024-08-15",
  "endDate": "2024-08-22",
  "budgetLevel": "Medium",
  "preferredActivities": ["Hiking", "Cultural Tours", "Wildlife Safari"],
  "preferredTerrains": ["Mountain", "Beach", "Cultural"],
  "activityPacing": "Normal",
  "multiCityAllowed": true
}
```

**Response JSON Example**:
```json
{
  "status": "success",
  "message": "Found 3 compatible group(s)",
  "totalSuggestions": 3,
  "hasCompatibleGroups": true,
  "suggestions": [
    {
      "groupId": "group_456",
      "tripName": "Sri Lanka Explorer",
      "compatibilityScore": 0.87,
      "currentMembers": 4,
      "maxMembers": 8,
      "baseCity": "Colombo",
      "startDate": "2024-08-15",
      "endDate": "2024-08-22",
      "budgetLevel": "Medium",
      "commonActivities": ["Hiking", "Cultural Tours"],
      "commonTerrains": ["Mountain", "Cultural"],
      "createdBy": "user_789"
    },
    {
      "groupId": "group_789",
      "tripName": "Cultural Heritage Tour",
      "compatibilityScore": 0.73,
      "currentMembers": 3,
      "maxMembers": 6,
      "baseCity": "Colombo",
      "startDate": "2024-08-15",
      "endDate": "2024-08-22",
      "budgetLevel": "Medium",
      "commonActivities": ["Cultural Tours"],
      "commonTerrains": ["Cultural"],
      "createdBy": "user_456"
    }
  ]
}
```

**Frontend Implementation**:
```javascript
const preCheckCompatibleGroups = async (preferences) => {
    try {
        const response = await fetch('/api/v1/public-pooling/pre-check', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: currentUser.id,
                baseCity: preferences.baseCity,
                startDate: preferences.startDate,
                endDate: preferences.endDate,
                budgetLevel: preferences.budgetLevel,
                preferredActivities: preferences.preferredActivities,
                preferredTerrains: preferences.preferredTerrains,
                activityPacing: preferences.activityPacing || 'Normal',
                multiCityAllowed: preferences.multiCityAllowed || true
            })
        });
        
        const result = await response.json();
        
        if (response.ok && result.hasCompatibleGroups) {
            // Show suggestions modal
            showPreCheckSuggestionsModal(result.suggestions);
            return result;
        } else if (response.ok && !result.hasCompatibleGroups) {
            // No compatible groups, proceed with creation
            showCreateGroupForm();
            return result;
        } else {
            throw new Error(result.message || 'Failed to check for compatible groups');
        }
    } catch (error) {
        console.error('Error in pre-check:', error);
        showErrorMessage('Failed to check for compatible groups. You can still create a new group.');
        return { hasCompatibleGroups: false, suggestions: [] };
    }
};

// Usage in trip planning form
const handleTripPlanningStart = async (formData) => {
    // First check for existing compatible groups
    const preCheckResult = await preCheckCompatibleGroups(formData);
    
    if (preCheckResult.hasCompatibleGroups) {
        // User will see suggestions modal and can choose to join or create new
        localStorage.setItem('pendingTripData', JSON.stringify(formData));
    } else {
        // No compatible groups, proceed with creation
        await createNewGroupWithTrip(formData);
    }
};
```

### 2. Enhanced Group Creation with Draft Support
**Endpoint**: `POST /api/v1/groups/with-trip`

**Purpose**: Creates a group with trip planning, supporting both private (immediate finalization) and public (draft mode for suggestions)

**Request JSON Example**:
```json
{
  "userId": "user_123",
  "tripName": "Sri Lanka Explorer",
  "startDate": "2024-08-15",
  "endDate": "2024-08-22",
  "baseCity": "Colombo",
  "arrivalTime": "14:30",
  "multiCityAllowed": true,
  "activityPacing": "Normal",
  "budgetLevel": "Medium",
  "preferredTerrains": ["Mountain", "Beach", "Cultural"],
  "preferredActivities": ["Hiking", "Cultural Tours", "Wildlife Safari"],
  "visibility": "public",
  "maxMembers": 8,
  "requiresApproval": false,
  "additionalPreferences": {
    "accommodation": "Mid-range",
    "transport": "Private"
  }
}
```

**Response JSON Example**:
```json
{
  "status": "success",
  "groupId": "group_123",
  "tripId": "trip_456",
  "message": "Group created successfully in draft mode",
  "isDraft": true,
  "nextSteps": [
    "Plan your trip using the trip planning interface",
    "Finalize your trip to get suggestions for similar groups",
    "Choose to join an existing group or continue with your own"
  ]
}
```

**Frontend Implementation**:
```javascript
const createGroupWithTrip = async (groupData, fromPreCheck = false) => {
    try {
        // Get current user data from frontend storage (Firebase user data)
        const currentUser = getCurrentUser(); // Your method to get current user
        
        const response = await fetch('/api/v1/groups/with-trip', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: currentUser.uid, // Firebase user ID
                userEmail: currentUser.email, // Firebase user email for creator name lookup
                tripName: groupData.tripName,
                startDate: groupData.startDate,
                endDate: groupData.endDate,
                baseCity: groupData.baseCity,
                arrivalTime: groupData.arrivalTime || "",
                multiCityAllowed: groupData.multiCityAllowed ?? true,
                activityPacing: groupData.activityPacing || "Normal",
                budgetLevel: groupData.budgetLevel || "Medium",
                preferredTerrains: groupData.preferredTerrains || [],
                preferredActivities: groupData.preferredActivities || [],
                visibility: groupData.visibility || "public",
                maxMembers: groupData.maxMembers || 6,
                requiresApproval: groupData.requiresApproval || false,
                additionalPreferences: groupData.additionalPreferences || {}
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Store group and trip IDs for later use
            localStorage.setItem('currentGroupId', result.groupId);
            localStorage.setItem('currentTripId', result.tripId);
            localStorage.setItem('groupStatus', result.isDraft ? 'draft' : 'finalized');
            
            if (fromPreCheck) {
                localStorage.removeItem('pendingTripData');
            }
            
            // Show success message
            showSuccessMessage(result.message);
            
            // Redirect to trip planning interface
            window.location.href = `/trip-planning/${result.tripId}?groupId=${result.groupId}&mode=${result.isDraft ? 'draft' : 'final'}`;
            
            return {
                success: true,
                groupId: result.groupId,
                tripId: result.tripId,
                isDraft: result.isDraft
            };
        } else {
            throw new Error(result.message || 'Failed to create group');
        }
    } catch (error) {
        console.error('Error creating group with trip:', error);
        showErrorMessage(error.message);
        return { success: false, error: error.message };
    }
};

// Usage after pre-check suggestions
const handleCreateNewAfterPreCheck = () => {
    const pendingData = JSON.parse(localStorage.getItem('pendingTripData') || '{}');
    createGroupWithTrip(pendingData, true);
};

// Usage for direct creation (skipping pre-check)
const handleDirectGroupCreation = (formData) => {
    createGroupWithTrip(formData, false);
};
```

### 3. Enhanced Trip Finalization with Suggestions
**Endpoint**: `POST /api/v1/groups/{groupId}/finalize-trip`

**Purpose**: Finalizes a trip with different actions: check suggestions, finalize directly, or join an existing group

**Request JSON Examples**:

**Check for suggestions (public groups only)**:
```json
{
  "userId": "user_123",
  "action": "checkSuggestions"
}
```

**Finalize without suggestions**:
```json
{
  "userId": "user_123",
  "action": "finalize"
}
```

**Join an existing group**:
```json
{
  "userId": "user_123",
  "action": "join",
  "targetGroupId": "group_456"
}
```

**Response JSON Examples**:

**Suggestions available**:
```json
{
  "status": "success",
  "action": "suggestions_found",
  "groupId": "group_123",
  "tripId": "trip_456",
  "message": "Found 2 compatible groups",
  "suggestions": [
    {
      "groupId": "group_789",
      "tripName": "Adventure Paradise",
      "compatibilityScore": 0.92,
      "currentMembers": 5,
      "maxMembers": 8,
      "baseCity": "Colombo",
      "startDate": "2024-08-15",
      "endDate": "2024-08-22",
      "commonDestinations": ["Sigiriya", "Kandy", "Ella"],
      "commonPreferences": ["Hiking", "Cultural Tours"]
    }
  ],
  "canFinalize": true,
  "canJoin": true
}
```

**Successfully finalized**:
```json
{
  "status": "success",
  "action": "finalized",
  "groupId": "group_123",
  "tripId": "trip_456",
  "message": "Trip finalized successfully",
  "redirectTo": "/groups/group_123"
}
```

**Successfully joined existing group**:
```json
{
  "status": "success",
  "action": "joined",
  "groupId": "group_456",
  "tripId": "trip_789",
  "message": "Successfully joined the group",
  "originalGroupDeleted": true,
  "redirectTo": "/groups/group_456"
}
```

**Frontend Implementation**:
```javascript
const finalizeTrip = async (groupId, action, targetGroupId = null) => {
    try {
        const response = await fetch(`/api/v1/groups/${groupId}/finalize-trip`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: currentUser.id,
                action: action,
                ...(targetGroupId && { targetGroupId })
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            switch (result.action) {
                case 'suggestions_found':
                    // Show suggestions modal
                    showFinalizationSuggestionsModal(result.suggestions, groupId);
                    break;
                    
                case 'finalized':
                    // Clear local storage and redirect
                    clearTripCreationData();
                    showSuccessMessage(result.message);
                    window.location.href = result.redirectTo;
                    break;
                    
                case 'joined':
                    // Clear local storage and redirect to new group
                    clearTripCreationData();
                    showSuccessMessage(result.message);
                    window.location.href = result.redirectTo;
                    break;
                    
                default:
                    console.warn('Unknown action:', result.action);
            }
            
            return result;
        } else {
            throw new Error(result.message || 'Failed to finalize trip');
        }
    } catch (error) {
        console.error('Error finalizing trip:', error);
        showErrorMessage(error.message);
        return { success: false, error: error.message };
    }
};

// Usage in trip planning completion
const handleTripPlanningComplete = async () => {
    const groupId = localStorage.getItem('currentGroupId');
    const groupStatus = localStorage.getItem('groupStatus');
    
    if (groupStatus === 'draft') {
        // For public draft groups, check for suggestions first
        await finalizeTrip(groupId, 'checkSuggestions');
    } else {
        // For private groups, finalize directly
        await finalizeTrip(groupId, 'finalize');
    }
};

// Usage for joining from suggestions
const handleJoinExistingGroup = async (targetGroupId) => {
    const currentGroupId = localStorage.getItem('currentGroupId');
    await finalizeTrip(currentGroupId, 'join', targetGroupId);
};

// Usage for finalizing without joining
const handleProceedWithOwnTrip = async () => {
    const groupId = localStorage.getItem('currentGroupId');
    await finalizeTrip(groupId, 'finalize');
};
```

### 4. Enhanced Public Groups with Advanced Filtering
**Endpoint**: `GET /api/v1/groups/public`

**Purpose**: Get public groups with optional filtering and compatibility scoring

**Request Parameters**:
```
GET /api/v1/groups/public?userId=user_123&baseCity=Colombo&startDate=2024-08-15&endDate=2024-08-22&budgetLevel=Medium&preferredActivities=Hiking,Cultural Tours
```

**Response JSON Example**:
```json
[
  {
    "groupId": "group_456",
    "tripId": "trip_789",
    "tripName": "Sri Lanka Cultural Explorer",
    "preferences": {
      "baseCity": "Colombo",
      "startDate": "2024-08-15",
      "endDate": "2024-08-22",
      "budgetLevel": "Medium",
      "preferredActivities": ["Cultural Tours", "Hiking", "Photography"],
      "preferredTerrains": ["Cultural", "Mountain"]
    },
    "collaboratorCount": 4,
    "maxMembers": 8,
    "createdAt": "2024-07-15T10:30:00Z",
    "baseCity": "Colombo",
    "startDate": "2024-08-15",
    "endDate": "2024-08-22",
    "budgetLevel": "Medium",
    "preferredActivities": ["Cultural Tours", "Hiking", "Photography"],
    "preferredTerrains": ["Cultural", "Mountain"],
    "activityPacing": "Normal",
    "status": "finalized",
    "compatibilityScore": 0.87
  },
  {
    "groupId": "group_789",
    "tripId": "trip_456",
    "tripName": "Adventure Paradise",
    "preferences": {
      "baseCity": "Colombo",
      "startDate": "2024-08-15",
      "endDate": "2024-08-22",
      "budgetLevel": "Medium",
      "preferredActivities": ["Hiking", "Wildlife Safari"],
      "preferredTerrains": ["Mountain", "Beach"]
    },
    "collaboratorCount": 3,
    "maxMembers": 6,
    "createdAt": "2024-07-14T15:45:00Z",
    "baseCity": "Colombo",
    "startDate": "2024-08-15",
    "endDate": "2024-08-22",
    "budgetLevel": "Medium",
    "preferredActivities": ["Hiking", "Wildlife Safari"],
    "preferredTerrains": ["Mountain", "Beach"],
    "activityPacing": "Fast",
    "status": "finalized",
    "compatibilityScore": 0.73
  }
]
```

**Frontend Implementation**:
```javascript
const getPublicGroups = async (filters = {}) => {
    try {
        const params = new URLSearchParams({
            userId: currentUser.id,
            ...filters
        });
        
        // Remove empty parameters
        for (const [key, value] of params.entries()) {
            if (!value || value === '' || (Array.isArray(value) && value.length === 0)) {
                params.delete(key);
            }
        }
        
        const response = await fetch(`/api/v1/groups/public?${params.toString()}`);
        const groups = await response.json();
        
        if (response.ok) {
            return {
                success: true,
                groups: groups,
                totalGroups: groups.length,
                hasCompatibilityScores: groups.some(g => g.compatibilityScore !== undefined)
            };
        } else {
            throw new Error(groups.message || 'Failed to fetch public groups');
        }
    } catch (error) {
        console.error('Error fetching public groups:', error);
        return {
            success: false,
            error: error.message,
            groups: []
        };
    }
};

// Usage with filtering
const loadFilteredPublicGroups = async (filterCriteria) => {
    const filters = {
        baseCity: filterCriteria.baseCity,
        startDate: filterCriteria.startDate,
        endDate: filterCriteria.endDate,
        budgetLevel: filterCriteria.budgetLevel,
        preferredActivities: filterCriteria.preferredActivities?.join(',')
    };
    
    const result = await getPublicGroups(filters);
    
    if (result.success) {
        renderPublicGroupsWithScores(result.groups);
        
        if (result.hasCompatibilityScores) {
            showCompatibilityInfo('Groups are sorted by compatibility with your preferences');
        }
    } else {
        showErrorMessage(result.error);
    }
};

// Usage without filtering (all public groups)
const loadAllPublicGroups = async () => {
    const result = await getPublicGroups();
    
    if (result.success) {
        renderPublicGroups(result.groups);
    } else {
        showErrorMessage(result.error);
    }
};

// Enhanced rendering with compatibility scores
const renderPublicGroupsWithScores = (groups) => {
    const container = document.getElementById('public-groups-container');
    
    container.innerHTML = groups.map(group => `
        <div class="group-card ${group.compatibilityScore ? 'has-score' : ''}">
            <div class="group-header">
                <h3>${group.tripName}</h3>
                ${group.compatibilityScore ? `
                    <div class="compatibility-badge score-${getScoreClass(group.compatibilityScore)}">
                        ${Math.round(group.compatibilityScore * 100)}% Match
                    </div>
                ` : ''}
            </div>
            
            <div class="group-details">
                <p><strong>Trip:</strong> ${group.tripName}</p>
                <p><strong>Members:</strong> ${group.collaboratorCount}/${group.maxMembers}</p>
                <p><strong>Destination:</strong> ${group.baseCity}</p>
                <p><strong>Dates:</strong> ${group.startDate} to ${group.endDate}</p>
                <p><strong>Budget:</strong> ${group.budgetLevel}</p>
                <p><strong>Activities:</strong> ${group.preferredActivities?.join(', ')}</p>
                <p><strong>Pacing:</strong> ${group.activityPacing}</p>
            </div>
            
            <div class="group-actions">
                <button class="btn-primary" onclick="requestToJoinGroup('${group.groupId}')">
                    Join Group
                </button>
                <button class="btn-secondary" onclick="viewGroupDetails('${group.groupId}')">
                    View Details
                </button>
            </div>
        </div>
    `).join('');
};

// Utility function for score styling
const getScoreClass = (score) => {
    if (score >= 0.8) return 'high';
    if (score >= 0.6) return 'medium';
    return 'low';
};
```

### 5. Advanced Group Filtering Interface

**HTML Filter Form**:
```html
<div class="filter-panel">
    <h3>Filter Public Groups</h3>
    
    <form id="group-filter-form" onsubmit="handleFilterSubmit(event)">
        <div class="filter-row">
            <div class="filter-group">
                <label for="filter-baseCity">Base City</label>
                <select id="filter-baseCity" name="baseCity">
                    <option value="">Any City</option>
                    <option value="Colombo">Colombo</option>
                    <option value="Kandy">Kandy</option>
                    <option value="Galle">Galle</option>
                    <option value="Jaffna">Jaffna</option>
                </select>
            </div>
            
            <div class="filter-group">
                <label for="filter-budgetLevel">Budget Level</label>
                <select id="filter-budgetLevel" name="budgetLevel">
                    <option value="">Any Budget</option>
                    <option value="Low">Low</option>
                    <option value="Medium">Medium</option>
                    <option value="High">High</option>
                </select>
            </div>
        </div>
        
        <div class="filter-row">
            <div class="filter-group">
                <label for="filter-startDate">Start Date</label>
                <input type="date" id="filter-startDate" name="startDate">
            </div>
            
            <div class="filter-group">
                <label for="filter-endDate">End Date</label>
                <input type="date" id="filter-endDate" name="endDate">
            </div>
        </div>
        
        <div class="filter-group">
            <label>Preferred Activities</label>
            <div class="checkbox-group">
                <label><input type="checkbox" name="preferredActivities" value="Hiking"> Hiking</label>
                <label><input type="checkbox" name="preferredActivities" value="Cultural Tours"> Cultural Tours</label>
                <label><input type="checkbox" name="preferredActivities" value="Wildlife Safari"> Wildlife Safari</label>
                <label><input type="checkbox" name="preferredActivities" value="Beach Activities"> Beach Activities</label>
                <label><input type="checkbox" name="preferredActivities" value="Photography"> Photography</label>
                <label><input type="checkbox" name="preferredActivities" value="Adventure Sports"> Adventure Sports</label>
            </div>
        </div>
        
        <div class="filter-actions">
            <button type="submit" class="btn-primary">Apply Filters</button>
            <button type="button" class="btn-secondary" onclick="clearFilters()">Clear All</button>
        </div>
    </form>
</div>

<div class="results-section">
    <div class="results-header">
        <h3>Available Groups</h3>
        <div class="sort-options">
            <label for="sort-by">Sort by:</label>
            <select id="sort-by" onchange="handleSortChange(this.value)">
                <option value="compatibility">Compatibility Score</option>
                <option value="members">Member Count</option>
                <option value="created">Creation Date</option>
                <option value="budget">Budget Level</option>
            </select>
        </div>
    </div>
    
    <div id="public-groups-container">
        <!-- Groups will be rendered here -->
    </div>
</div>

<script>
const handleFilterSubmit = (event) => {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const filters = {};
    
    // Get single values
    filters.baseCity = formData.get('baseCity');
    filters.budgetLevel = formData.get('budgetLevel');
    filters.startDate = formData.get('startDate');
    filters.endDate = formData.get('endDate');
    
    // Get multiple values (activities)
    const activities = formData.getAll('preferredActivities');
    if (activities.length > 0) {
        filters.preferredActivities = activities;
    }
    
    // Apply filters
    loadFilteredPublicGroups(filters);
};

const clearFilters = () => {
    document.getElementById('group-filter-form').reset();
    loadAllPublicGroups();
};

const handleSortChange = (sortBy) => {
    const container = document.getElementById('public-groups-container');
    const cards = Array.from(container.children);
    
    cards.sort((a, b) => {
        switch (sortBy) {
            case 'compatibility':
                const scoreA = parseFloat(a.querySelector('.compatibility-badge')?.textContent) || 0;
                const scoreB = parseFloat(b.querySelector('.compatibility-badge')?.textContent) || 0;
                return scoreB - scoreA;
                
            case 'members':
                const membersA = parseInt(a.querySelector('.group-details p:nth-child(2)')?.textContent.split('/')[0]) || 0;
                const membersB = parseInt(b.querySelector('.group-details p:nth-child(2)')?.textContent.split('/')[0]) || 0;
                return membersB - membersA;
                
            case 'created':
                // Would need creation date in data attributes for proper sorting
                return 0;
                
            case 'budget':
                const budgetA = a.querySelector('.group-details p:nth-child(5)')?.textContent || '';
                const budgetB = b.querySelector('.group-details p:nth-child(5)')?.textContent || '';
                const budgetOrder = {'Low': 1, 'Medium': 2, 'High': 3};
                return budgetOrder[budgetA] - budgetOrder[budgetB];
                
            default:
                return 0;
        }
    });
    
    // Re-append sorted cards
    cards.forEach(card => container.appendChild(card));
};
</script>
```

## 🎯 Complete Hybrid Workflow Examples

### Scenario 1: Pre-Check Before Creating Group

```javascript
// Complete workflow starting with pre-check
const startTripPlanning = async (userPreferences) => {
    try {
        // Step 1: Pre-check for compatible groups
        const preCheckResult = await preCheckCompatibleGroups(userPreferences);
        
        if (preCheckResult.hasCompatibleGroups) {
            // Show modal with existing options
            showPreCheckModal(preCheckResult.suggestions, userPreferences);
        } else {
            // No compatible groups, proceed with creation
            await createGroupWithTrip(userPreferences);
        }
    } catch (error) {
        console.error('Error in trip planning start:', error);
        showErrorMessage('Failed to start trip planning');
    }
};

// Pre-check modal component
const showPreCheckModal = (suggestions, userPreferences) => {
    const modal = document.createElement('div');
    modal.className = 'pre-check-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h2>Great news! We found ${suggestions.length} compatible group(s) 🎉</h2>
                <p>You can join an existing group or create your own.</p>
            </div>
            
            <div class="suggestions-list">
                ${suggestions.map(group => `
                    <div class="suggestion-card">
                        <div class="suggestion-header">
                            <h3>${group.tripName}</h3>
                            <div class="compatibility-badge score-${getScoreClass(group.compatibilityScore)}">
                                ${Math.round(group.compatibilityScore * 100)}% Match
                            </div>
                        </div>
                        
                        <div class="suggestion-details">
                            <p><strong>Trip:</strong> ${group.tripName}</p>
                            <p><strong>Members:</strong> ${group.currentMembers}/${group.maxMembers}</p>
                            <p><strong>Dates:</strong> ${group.startDate} to ${group.endDate}</p>
                            <p><strong>Common Interests:</strong> ${group.commonActivities?.join(', ')}</p>
                        </div>
                        
                        <button class="btn-primary" onclick="joinExistingGroupFromPreCheck('${group.groupId}')">
                            Join This Group
                        </button>
                    </div>
                `).join('')}
            </div>
            
            <div class="modal-actions">
                <button class="btn-secondary" onclick="createNewAfterPreCheck()">
                    Create My Own Group
                </button>
                <button class="btn-outline" onclick="closePreCheckModal()">
                    Let Me Think
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Store user preferences for later use
    localStorage.setItem('pendingTripData', JSON.stringify(userPreferences));
};

// Handle joining from pre-check
const joinExistingGroupFromPreCheck = async (targetGroupId) => {
    try {
        // Since we haven't created a group yet, just join directly
        const response = await fetch(`/api/v1/groups/${targetGroupId}/join`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.id,
                userEmail: currentUser.email,
                userName: currentUser.name,
                message: 'Found your group through compatibility matching!'
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            closePreCheckModal();
            showSuccessMessage('Successfully joined the group!');
            window.location.href = `/groups/${targetGroupId}`;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        showErrorMessage('Failed to join group: ' + error.message);
    }
};

// Handle creating new after seeing suggestions
const createNewAfterPreCheck = () => {
    const pendingData = JSON.parse(localStorage.getItem('pendingTripData'));
    closePreCheckModal();
    createGroupWithTrip(pendingData, true);
};
```

### Scenario 2: Suggestions During Trip Finalization

```javascript
// Trip planning completion workflow
const completeTripPlanning = async () => {
    const groupId = localStorage.getItem('currentGroupId');
    const groupStatus = localStorage.getItem('groupStatus');
    const visibility = localStorage.getItem('groupVisibility');
    
    if (visibility === 'public' && groupStatus === 'draft') {
        // For public draft groups, check for suggestions
        await showFinalizationOptions(groupId);
    } else {
        // For private groups, finalize directly
        await finalizeTrip(groupId, 'finalize');
    }
};

const showFinalizationOptions = async (groupId) => {
    // First check if there are suggestions
    const suggestions = await finalizeTrip(groupId, 'checkSuggestions');
    
    if (suggestions.suggestions && suggestions.suggestions.length > 0) {
        showFinalizationModal(suggestions.suggestions, groupId);
    } else {
        // No suggestions, finalize directly
        await finalizeTrip(groupId, 'finalize');
    }
};

const showFinalizationModal = (suggestions, currentGroupId) => {
    const modal = document.createElement('div');
    modal.className = 'finalization-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h2>Before you finalize... 🤔</h2>
                <p>We found ${suggestions.length} similar group(s) that might interest you!</p>
            </div>
            
            <div class="suggestions-comparison">
                <div class="your-trip">
                    <h3>Your Trip</h3>
                    <div class="trip-card current">
                        <p>You'll be the group leader</p>
                        <p>Full control over itinerary</p>
                        <p>Start fresh with your preferences</p>
                    </div>
                </div>
                
                <div class="suggested-trips">
                    <h3>Similar Groups</h3>
                    ${suggestions.map(group => `
                        <div class="trip-card suggested">
                            <div class="suggestion-header">
                                <h4>${group.tripName}</h4>
                                <span class="compatibility-score">${Math.round(group.compatibilityScore * 100)}% Match</span>
                            </div>
                            <p><strong>Members:</strong> ${group.currentMembers}/${group.maxMembers}</p>
                            <p><strong>Common:</strong> ${group.commonDestinations?.join(', ')}</p>
                            <button class="btn-primary btn-sm" onclick="handleJoinFromFinalization('${group.groupId}')">
                                Join Group
                            </button>
                        </div>
                    `).join('')}
                </div>
            </div>
            
            <div class="modal-actions">
                <button class="btn-primary" onclick="handleFinalizeOwnTrip('${currentGroupId}')">
                    Continue with My Trip
                </button>
                <button class="btn-secondary" onclick="closeFinalizationModal()">
                    Let Me Decide Later
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
};

const handleJoinFromFinalization = async (targetGroupId) => {
    const currentGroupId = localStorage.getItem('currentGroupId');
    
    try {
        const result = await finalizeTrip(currentGroupId, 'join', targetGroupId);
        
        if (result.success !== false) {
            closeFinalizationModal();
            clearTripCreationData();
            showSuccessMessage('Successfully joined the group! Your draft has been discarded.');
            window.location.href = `/groups/${targetGroupId}`;
        }
    } catch (error) {
        showErrorMessage('Failed to join group: ' + error.message);
    }
};

const handleFinalizeOwnTrip = async (groupId) => {
    try {
        const result = await finalizeTrip(groupId, 'finalize');
        
        if (result.success !== false) {
            closeFinalizationModal();
            clearTripCreationData();
            showSuccessMessage('Trip finalized successfully!');
            window.location.href = `/groups/${groupId}`;
        }
    } catch (error) {
        showErrorMessage('Failed to finalize trip: ' + error.message);
    }
};
```

## 🔧 Utility Functions and Helpers

### State Management Utilities

```javascript
// Trip creation state management
class TripCreationManager {
    constructor() {
        this.state = {
            currentStep: 'preferences',
            preferences: {},
            groupData: {},
            suggestionsShown: false,
            pendingAction: null
        };
    }
    
    // Save current state to localStorage
    saveState() {
        localStorage.setItem('tripCreationState', JSON.stringify(this.state));
    }
    
    // Load state from localStorage
    loadState() {
        const saved = localStorage.getItem('tripCreationState');
        if (saved) {
            this.state = { ...this.state, ...JSON.parse(saved) };
        }
        return this.state;
    }
    
    // Clear all trip creation data
    clearState() {
        localStorage.removeItem('tripCreationState');
        localStorage.removeItem('pendingTripData');
        localStorage.removeItem('currentGroupId');
        localStorage.removeItem('groupStatus');
        localStorage.removeItem('groupVisibility');
        this.state = {
            currentStep: 'preferences',
            preferences: {},
            groupData: {},
            suggestionsShown: false,
            pendingAction: null
        };
    }
    
    // Update specific state properties
    updateState(updates) {
        this.state = { ...this.state, ...updates };
        this.saveState();
    }
    
    // Check if we should show suggestions
    shouldShowSuggestions() {
        return !this.state.suggestionsShown && 
               this.state.currentStep === 'finalization';
    }
}

// Global instance
const tripManager = new TripCreationManager();

// Helper function to get user preferences
const getUserProfile = async (userId) => {
    try {
        const response = await fetch(`/api/v1/users/${userId}/profile`);
        const profile = await response.json();
        
        return {
            preferredBaseCity: profile.preferredBaseCity || '',
            budgetLevel: profile.budgetLevel || 'Medium',
            preferredActivities: profile.preferredActivities || [],
            preferredTerrains: profile.preferredTerrains || []
        };
    } catch (error) {
        console.error('Error fetching user profile:', error);
        return {
            preferredBaseCity: '',
            budgetLevel: 'Medium',
            preferredActivities: [],
            preferredTerrains: []
        };
    }
};

// Format date range for display
const formatDateRange = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    const options = { month: 'short', day: 'numeric' };
    const startFormatted = start.toLocaleDateString('en-US', options);
    const endFormatted = end.toLocaleDateString('en-US', options);
    
    if (start.getFullYear() !== end.getFullYear()) {
        return `${startFormatted}, ${start.getFullYear()} - ${endFormatted}, ${end.getFullYear()}`;
    } else if (start.getMonth() !== end.getMonth()) {
        return `${startFormatted} - ${endFormatted}, ${start.getFullYear()}`;
    } else {
        return `${start.getDate()}-${end.getDate()} ${start.toLocaleDateString('en-US', { month: 'short', year: 'numeric' })}`;
    }
};

// Get compatibility score CSS class
const getScoreClass = (score) => {
    if (score >= 0.8) return 'high';
    if (score >= 0.6) return 'medium';
    return 'low';
};

// Clear trip creation data
const clearTripCreationData = () => {
    tripManager.clearState();
};
```

### Modal Management System

```javascript
// Modal management utility
class ModalManager {
    constructor() {
        this.activeModals = [];
        this.setupEventListeners();
    }
    
    setupEventListeners() {
        // Close modal on ESC key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.activeModals.length > 0) {
                this.closeTopModal();
            }
        });
        
        // Close modal on backdrop click
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-backdrop')) {
                this.closeTopModal();
            }
        });
    }
    
    showModal(modalElement) {
        // Add backdrop
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop';
        backdrop.appendChild(modalElement);
        
        document.body.appendChild(backdrop);
        document.body.style.overflow = 'hidden';
        
        this.activeModals.push(backdrop);
        
        // Animate in
        requestAnimationFrame(() => {
            backdrop.classList.add('show');
        });
    }
    
    closeTopModal() {
        if (this.activeModals.length === 0) return;
        
        const modal = this.activeModals.pop();
        modal.classList.add('closing');
        
        setTimeout(() => {
            if (modal.parentNode) {
                modal.parentNode.removeChild(modal);
            }
            
            if (this.activeModals.length === 0) {
                document.body.style.overflow = '';
            }
        }, 300);
    }
    
    closeAllModals() {
        while (this.activeModals.length > 0) {
            this.closeTopModal();
        }
    }
}

// Global modal manager
const modalManager = new ModalManager();

// Modal helper functions
const closePreCheckModal = () => modalManager.closeTopModal();
const closeFinalizationModal = () => modalManager.closeTopModal();
const closeFilterDrawer = () => modalManager.closeTopModal();
```

### Notification System

```javascript
// Notification utility
class NotificationManager {
    constructor() {
        this.container = this.createContainer();
        this.notifications = [];
    }
    
    createContainer() {
        const container = document.createElement('div');
        container.className = 'notification-container';
        container.innerHTML = '<div class="notifications-list"></div>';
        document.body.appendChild(container);
        return container;
    }
    
    show(message, type = 'info', duration = 5000) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        
        const icon = this.getIcon(type);
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">${icon}</span>
                <span class="notification-message">${message}</span>
                <button class="notification-close" onclick="this.parentElement.parentElement.remove()">×</button>
            </div>
        `;
        
        const list = this.container.querySelector('.notifications-list');
        list.appendChild(notification);
        
        // Animate in
        requestAnimationFrame(() => {
            notification.classList.add('show');
        });
        
        // Auto remove
        if (duration > 0) {
            setTimeout(() => {
                this.removeNotification(notification);
            }, duration);
        }
        
        this.notifications.push(notification);
        return notification;
    }
    
    removeNotification(notification) {
        notification.classList.add('removing');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
            
            const index = this.notifications.indexOf(notification);
            if (index > -1) {
                this.notifications.splice(index, 1);
            }
        }, 300);
    }
    
    getIcon(type) {
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        return icons[type] || icons.info;
    }
    
    clear() {
        this.notifications.forEach(notification => {
            this.removeNotification(notification);
        });
    }
}

// Global notification manager
const notificationManager = new NotificationManager();

// Helper functions
const showSuccessMessage = (message) => notificationManager.show(message, 'success');
const showErrorMessage = (message) => notificationManager.show(message, 'error');
const showWarningMessage = (message) => notificationManager.show(message, 'warning');
const showInfoMessage = (message) => notificationManager.show(message, 'info');
```

### Form Validation Utilities

```javascript
// Form validation helpers
const validateTripForm = (formData) => {
    const errors = [];
    
    // Required fields
    if (!formData.tripName?.trim()) {
        errors.push('Trip name is required');
    }
    
    if (!formData.baseCity?.trim()) {
        errors.push('Base city is required');
    }
    
    if (!formData.startDate) {
        errors.push('Start date is required');
    }
    
    if (!formData.endDate) {
        errors.push('End date is required');
    }
    
    // Date validation
    if (formData.startDate && formData.endDate) {
        const startDate = new Date(formData.startDate);
        const endDate = new Date(formData.endDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        if (startDate < today) {
            errors.push('Start date cannot be in the past');
        }
        
        if (endDate < startDate) {
            errors.push('End date must be after start date');
        }
        
        const diffDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
        if (diffDays > 30) {
            errors.push('Trip duration cannot exceed 30 days');
        }
    }
    
    // Member count validation
    if (formData.maxMembers && (formData.maxMembers < 1 || formData.maxMembers > 20)) {
        errors.push('Maximum members must be between 1 and 20');
    }
    
    return {
        isValid: errors.length === 0,
        errors
    };
};

// Filter validation
const validateFilters = (filters) => {
    const errors = [];
    
    if (filters.startDate && filters.endDate) {
        const startDate = new Date(filters.startDate);
        const endDate = new Date(filters.endDate);
        
        if (endDate < startDate) {
            errors.push('End date must be after start date');
        }
    }
    
    if (filters.maxMembers && filters.maxMembers < 1) {
        errors.push('Maximum members must be at least 1');
    }
    
    return {
        isValid: errors.length === 0,
        errors
    };
};

// Display form errors
const displayFormErrors = (errors) => {
    const errorContainer = document.getElementById('form-errors');
    if (!errorContainer) return;
    
    if (errors.length === 0) {
        errorContainer.style.display = 'none';
        return;
    }
    
    errorContainer.innerHTML = `
        <div class="error-list">
            <h4>Please fix the following errors:</h4>
            <ul>
                ${errors.map(error => `<li>${error}</li>`).join('')}
            </ul>
        </div>
    `;
    errorContainer.style.display = 'block';
    
    // Scroll to errors
    errorContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
};
```

## 🎨 Complete CSS Styles

```css
/* Enhanced Pooling Service Styles */

/* Modal Styles */
.modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.modal-backdrop.show {
    opacity: 1;
}

.modal-backdrop.closing {
    opacity: 0;
}

.modal-content {
    background: white;
    border-radius: 12px;
    width: 100%;
    max-width: 700px;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    transform: translateY(20px);
    transition: transform 0.3s ease;
}

.modal-backdrop.show .modal-content {
    transform: translateY(0);
}

/* Notification Styles */
.notification-container {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 2000;
    pointer-events: none;
}

.notifications-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.notification {
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    pointer-events: auto;
    transform: translateX(100%);
    transition: transform 0.3s ease;
    min-width: 300px;
    max-width: 400px;
}

.notification.show {
    transform: translateX(0);
}

.notification.removing {
    transform: translateX(100%);
}

.notification-content {
    display: flex;
    align-items: center;
    padding: 16px;
    gap: 12px;
}

.notification-success {
    border-left: 4px solid #4CAF50;
}

.notification-error {
    border-left: 4px solid #f44336;
}

.notification-warning {
    border-left: 4px solid #FF9800;
}

.notification-info {
    border-left: 4px solid #2196F3;
}

.notification-message {
    flex: 1;
    font-size: 14px;
    line-height: 1.4;
}

.notification-close {
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    opacity: 0.5;
    transition: opacity 0.2s ease;
}

.notification-close:hover {
    opacity: 1;
}

/* Button Styles */
.btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
    background: #f8f9fa;
    color: #495057;
    border: 1px solid #dee2e6;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-secondary:hover {
    background: #e9ecef;
    transform: translateY(-1px);
}

.btn-outline {
    background: transparent;
    color: #667eea;
    border: 2px solid #667eea;
    padding: 10px 22px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-outline:hover {
    background: #667eea;
    color: white;
    transform: translateY(-1px);
}

/* Form Styles */
.touch-input {
    width: 100%;
    padding: 12px 16px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 16px;
    transition: border-color 0.2s ease;
}

.touch-input:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-section {
    margin-bottom: 24px;
}

.filter-section label {
    display: block;
    font-weight: 600;
    margin-bottom: 8px;
    color: #333;
}

.button-group {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.filter-button {
    padding: 10px 16px;
    border: 2px solid #e0e0e0;
    background: white;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.filter-button.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
}

.tag-selector {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.activity-tag {
    padding: 8px 12px;
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 20px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.activity-tag.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
}

/* Card Styles */
.discovery-card, .suggestion-card {
    background: white;
    border: 1px solid #e0e0e0;
    border-radius: 12px;
    padding: 20px;
    transition: all 0.3s ease;
    cursor: pointer;
}

.discovery-card:hover, .suggestion-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
}

.discovery-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin: 20px 0;
}

/* Mobile Responsive */
@media (max-width: 768px) {
    .modal-content {
        margin: 10px;
        border-radius: 8px;
    }
    
    .discovery-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }
    
    .button-group {
        flex-direction: column;
    }
    
    .filter-button {
        width: 100%;
        text-align: center;
    }
    
    .notification {
        min-width: 280px;
        margin: 0 10px;
    }
    
    .btn-primary, .btn-secondary, .btn-outline {
        width: 100%;
        margin-bottom: 10px;
    }
}
```

## 🧪 Testing Examples

### Unit Testing Functions

```javascript
// Test the pre-check functionality
const testPreCheckFlow = async () => {
    console.log('Testing pre-check flow...');
    
    const testPreferences = {
        tripName: "Sri Lanka Adventure Test",
        baseCity: "Colombo",
        startDate: "2024-12-01",
        endDate: "2024-12-07",
        budgetLevel: "Medium",
        preferredActivities: ["Hiking", "Cultural Tours"],
        preferredTerrains: ["Mountain", "Beach"],
        multiCityAllowed: true,
        activityPacing: "Normal"
    };
    
    try {
        // Test pre-check
        const result = await preCheckCompatibleGroups(testPreferences);
        console.log('Pre-check result:', result);
        
        if (result.hasCompatibleGroups) {
            console.log(`✅ Found ${result.suggestions.length} compatible groups`);
            result.suggestions.forEach((group, index) => {
                console.log(`Group ${index + 1}: ${group.tripName} (${Math.round(group.compatibilityScore * 100)}% match)`);
            });
        } else {
            console.log('❌ No compatible groups found');
        }
        
        return result;
    } catch (error) {
        console.error('❌ Pre-check test failed:', error);
        throw error;
    }
};

// Test group filtering
const testGroupFiltering = async () => {
    console.log('Testing group filtering...');
    
    const testFilters = {
        baseCity: "Colombo",
        budgetLevel: "Medium",
        startDate: "2024-12-01",
        preferredActivities: ["Hiking"]
    };
    
    try {
        const result = await getPublicGroups(testFilters);
        console.log('Filter result:', result);
        
        if (result.success) {
            console.log(`✅ Found ${result.groups.length} filtered groups`);
            result.groups.forEach((group, index) => {
                console.log(`Group ${index + 1}: ${group.tripName} in ${group.baseCity}`);
            });
        } else {
            console.log('❌ Filtering failed');
        }
        
        return result;
    } catch (error) {
        console.error('❌ Filtering test failed:', error);
        throw error;
    }
};

// Integration test
const runIntegrationTest = async () => {
    console.log('🧪 Running integration test...');
    
    try {
        // 1. Test pre-check
        await testPreCheckFlow();
        
        // 2. Test filtering
        await testGroupFiltering();
        
        // 3. Test state management
        tripManager.updateState({ currentStep: 'testing' });
        const state = tripManager.loadState();
        console.log('State management test:', state.currentStep === 'testing' ? '✅' : '❌');
        
        console.log('🎉 All tests passed!');
    } catch (error) {
        console.error('❌ Integration test failed:', error);
    }
};

// Run tests (call this in browser console)
// runIntegrationTest();
```

## 📱 Mobile-Optimized Components

### Responsive Pre-Check Modal

```css
/* Mobile-first responsive design */
.pre-check-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.8);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.modal-content {
    background: white;
    border-radius: 12px;
    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
    animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.suggestion-card {
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    transition: all 0.2s ease;
}

.suggestion-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.compatibility-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
    color: white;
}

.compatibility-badge.score-high {
    background: linear-gradient(135deg, #4CAF50, #45a049);
}

.compatibility-badge.score-medium {
    background: linear-gradient(135deg, #FF9800, #F57C00);
}

.compatibility-badge.score-low {
    background: linear-gradient(135deg, #757575, #616161);
}

/* Mobile optimizations */
@media (max-width: 768px) {
    .pre-check-modal {
        padding: 10px;
    }
    
    .modal-content {
        border-radius: 8px;
        max-height: 95vh;
    }
    
    .suggestions-list {
        padding: 10px;
    }
    
    .suggestion-card {
        padding: 12px;
        margin-bottom: 8px;
    }
    
    .modal-actions {
        flex-direction: column;
        gap: 8px;
    }
    
    .btn-primary, .btn-secondary, .btn-outline {
        width: 100%;
        padding: 12px;
    }
}
```

### Touch-Friendly Filter Interface

```html
<div class="mobile-filter-drawer">
    <div class="filter-header">
        <h3>Filter Groups</h3>
        <button class="close-filters" onclick="closeFilterDrawer()">✕</button>
    </div>
    
    <div class="filter-content">
        <!-- Touch-friendly filter controls -->
        <div class="filter-section">
            <label>When are you traveling?</label>
            <div class="date-picker-row">
                <input type="date" class="touch-input" name="startDate" placeholder="Start Date">
                <input type="date" class="touch-input" name="endDate" placeholder="End Date">
            </div>
        </div>
        
        <div class="filter-section">
            <label>Budget Level</label>
            <div class="button-group">
                <button class="filter-button" data-value="Low">💰 Low</button>
                <button class="filter-button" data-value="Medium">💰💰 Medium</button>
                <button class="filter-button" data-value="High">💰💰💰 High</button>
            </div>
        </div>
        
        <div class="filter-section">
            <label>Activities You Love</label>
            <div class="tag-selector">
                <button class="activity-tag" data-activity="Hiking">🥾 Hiking</button>
                <button class="activity-tag" data-activity="Cultural Tours">🏛️ Cultural</button>
                <button class="activity-tag" data-activity="Wildlife Safari">🦁 Safari</button>
                <button class="activity-tag" data-activity="Beach Activities">🏖️ Beach</button>
                <button class="activity-tag" data-activity="Photography">📸 Photo</button>
                <button class="activity-tag" data-activity="Adventure Sports">🧗 Adventure</button>
            </div>
        </div>
    </div>
    
    <div class="filter-actions">
        <button class="btn-primary btn-block" onclick="applyMobileFilters()">
            Apply Filters
        </button>
        <button class="btn-secondary btn-block" onclick="clearMobileFilters()">
            Clear All
        </button>
    </div>
</div>
```

### 3. Finalize Trip Decision (NEW)
**Endpoint**: `POST /api/v1/groups/{groupId}/finalize-trip`

**Purpose**: Handles user's decision to finalize their trip or join an existing group

**Request Schema**:
```typescript
interface FinalizeTripRequest {
    userId: string;
    action: "finalize" | "join";
    targetGroupId?: string;  // Required if action is "join"
}
```

**Response Schema**:
```typescript
interface FinalizeTripResponse {
    status: "success" | "error";
    message: string;
    groupId: string;
    tripId: string;
    action: "finalized" | "joined";
    success: boolean;
}
```

**Frontend Implementation**:
```javascript
const finalizeTrip = async (groupId, action, targetGroupId = null) => {
    try {
        const response = await fetch(`/api/v1/groups/${groupId}/finalize-trip`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: currentUser.id,
                action: action,
                targetGroupId: targetGroupId
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            return {
                success: true,
                action: result.action,
                groupId: result.groupId,
                tripId: result.tripId,
                message: result.message
            };
        } else {
            throw new Error(result.message || 'Failed to finalize trip');
        }
    } catch (error) {
        console.error('Error finalizing trip:', error);
        return {
            success: false,
            error: error.message
        };
    }
};

// Usage Examples
const joinExistingGroup = async (targetGroupId) => {
    const result = await finalizeTrip(currentGroupId, 'join', targetGroupId);
    
    if (result.success) {
        showNotification('Successfully joined the group!', 'success');
        // Redirect to the new group
        window.location.href = `/groups/${result.groupId}`;
    } else {
        showNotification(result.error, 'error');
    }
};

const proceedWithOwnTrip = async () => {
    const result = await finalizeTrip(currentGroupId, 'finalize');
    
    if (result.success) {
        showNotification('Trip finalized successfully!', 'success');
        // Redirect to active group page
        window.location.href = `/groups/${result.groupId}`;
    } else {
        showNotification(result.error, 'error');
    }
};
```

## 🎯 Complete Public Pooling Workflow

### Step 1: Create Group with Trip
```javascript
// User fills out the form and creates a group with trip
const createPublicPoolingGroup = async (formData) => {
    const result = await createGroupWithTrip({
        userId: currentUser.id,
        tripName: formData.tripName,
        startDate: formData.startDate,
        endDate: formData.endDate,
        baseCity: formData.baseCity,
        // ... other fields
    });
    
    if (result.success) {
        // Store group and trip IDs for later use
        localStorage.setItem('currentGroupId', result.groupId);
        localStorage.setItem('currentTripId', result.tripId);
        
        // Redirect to trip planning
        window.location.href = `/trip-planning/${result.tripId}?groupId=${result.groupId}`;
    }
};
```

### Step 2: Trip Planning Integration
```javascript
// After user completes trip planning, show the "Save Trip" button
const handleTripPlanningComplete = () => {
    // Show the save trip button
    document.getElementById('save-trip-btn').style.display = 'block';
    document.getElementById('save-trip-btn').onclick = handleSaveTrip;
};

// When user clicks "Save Trip"
const handleSaveTrip = async () => {
    const groupId = localStorage.getItem('currentGroupId');
    
    // Get trip suggestions
    const suggestions = await getTripSuggestions(groupId, currentUser.id);
    
    if (suggestions.success) {
        if (suggestions.suggestions.length > 0) {
            // Show suggestions modal
            showSuggestionsModal(suggestions.suggestions);
        } else {
            // No suggestions, proceed with finalization
            showFinalizationModal();
        }
    } else {
        showNotification(suggestions.error, 'error');
    }
};
```

### Step 3: Handle User Decision
```javascript
// User chooses to join an existing group
const joinExistingGroup = async (targetGroupId) => {
    const groupId = localStorage.getItem('currentGroupId');
    
    const result = await finalizeTrip(groupId, 'join', targetGroupId);
    
    if (result.success) {
        // Clear local storage
        localStorage.removeItem('currentGroupId');
        localStorage.removeItem('currentTripId');
        
        // Redirect to the joined group
        window.location.href = `/groups/${result.groupId}`;
    } else {
        showNotification(result.error, 'error');
    }
};

// User chooses to proceed with their own trip
const proceedWithOwnTrip = async () => {
    const groupId = localStorage.getItem('currentGroupId');
    
    const result = await finalizeTrip(groupId, 'finalize');
    
    if (result.success) {
        // Clear local storage
        localStorage.removeItem('currentGroupId');
        localStorage.removeItem('currentTripId');
        
        // Redirect to the active group
        window.location.href = `/groups/${result.groupId}`;
    } else {
        showNotification(result.error, 'error');
    }
};
```

## 🎨 UI Components

### 1. Create Group Form
```html
<form id="create-group-form" onsubmit="handleCreateGroup(event)">
    <div class="form-group">
        <label for="tripName">Trip Name</label>
        <input type="text" id="tripName" name="tripName" required>
    </div>
    
    <div class="form-row">
        <div class="form-group">
            <label for="startDate">Start Date</label>
            <input type="date" id="startDate" name="startDate" required>
        </div>
        <div class="form-group">
            <label for="endDate">End Date</label>
            <input type="date" id="endDate" name="endDate" required>
        </div>
    </div>
    
    <div class="form-group">
        <label for="baseCity">Base City</label>
        <input type="text" id="baseCity" name="baseCity" required>
    </div>
    
    <div class="form-group">
        <label for="arrivalTime">Arrival Time (Optional)</label>
        <input type="time" id="arrivalTime" name="arrivalTime">
    </div>
    
    <div class="form-group">
        <label for="activityPacing">Activity Pacing</label>
        <select id="activityPacing" name="activityPacing">
            <option value="Relaxed">Relaxed</option>
            <option value="Normal" selected>Normal</option>
            <option value="Fast">Fast</option>
        </select>
    </div>
    
    <div class="form-group">
        <label for="budgetLevel">Budget Level</label>
        <select id="budgetLevel" name="budgetLevel">
            <option value="Low">Low</option>
            <option value="Medium" selected>Medium</option>
            <option value="High">High</option>
        </select>
    </div>
    
    <div class="form-group">
        <label for="maxMembers">Maximum Members</label>
        <input type="number" id="maxMembers" name="maxMembers" min="2" max="20" value="6">
    </div>
    
    <div class="form-group">
        <label>Preferred Terrains</label>
        <div class="checkbox-group">
            <label><input type="checkbox" value="Beach"> Beach</label>
            <label><input type="checkbox" value="Mountain"> Mountain</label>
            <label><input type="checkbox" value="Historical"> Historical</label>
            <label><input type="checkbox" value="National Park"> National Park</label>
            <label><input type="checkbox" value="Cultural"> Cultural</label>
        </div>
    </div>
    
    <div class="form-group">
        <label>Preferred Activities</label>
        <div class="checkbox-group">
            <label><input type="checkbox" value="Hiking"> Hiking</label>
            <label><input type="checkbox" value="Cultural Tours"> Cultural Tours</label>
            <label><input type="checkbox" value="Wildlife Safari"> Wildlife Safari</label>
            <label><input type="checkbox" value="Photography"> Photography</label>
            <label><input type="checkbox" value="Beach Activities"> Beach Activities</label>
        </div>
    </div>
    
    <button type="submit" class="btn-primary">Create Group & Start Planning</button>
</form>

<script>
const handleCreateGroup = async (event) => {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());
    
    // Get selected terrains and activities
    data.preferredTerrains = Array.from(document.querySelectorAll('input[type="checkbox"]:checked'))
        .filter(cb => cb.closest('.form-group').querySelector('label').textContent.includes('Terrains'))
        .map(cb => cb.value);
    
    data.preferredActivities = Array.from(document.querySelectorAll('input[type="checkbox"]:checked'))
        .filter(cb => cb.closest('.form-group').querySelector('label').textContent.includes('Activities'))
        .map(cb => cb.value);
    
    data.userId = currentUser.id;
    
    const result = await createGroupWithTrip(data);
    
    if (result.success) {
        showNotification('Group created successfully!', 'success');
    } else {
        showNotification(result.error, 'error');
    }
};
</script>
```

### 2. Trip Suggestions Modal
```html
<div id="suggestions-modal" class="modal" style="display: none;">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Similar Groups Found! 🎉</h2>
            <span class="close" onclick="closeModal()">&times;</span>
        </div>
        <div class="modal-body">
            <p>We found groups with similar trip plans. You can join an existing group or continue with your own.</p>
            <div id="suggestions-container"></div>
        </div>
        <div class="modal-footer">
            <button class="btn-secondary" onclick="proceedWithOwnTrip()">
                Continue with My Trip
            </button>
            <button class="btn-outline" onclick="closeModal()">
                Let Me Think
            </button>
        </div>
    </div>
</div>

<script>
const showSuggestionsModal = (suggestions) => {
    const container = document.getElementById('suggestions-container');
    
    container.innerHTML = suggestions.map(group => `
        <div class="suggestion-card">
            <div class="suggestion-header">
                <h3>${group.tripName}</h3>
                <div class="compatibility-badge">
                    ${Math.round(group.compatibilityScore * 100)}% Match
                </div>
            </div>
            <div class="suggestion-details">
                <div class="detail-row">
                    <span class="label">Trip:</span>
                    <span class="value">${group.tripName}</span>
                </div>
                <div class="detail-row">
                    <span class="label">Members:</span>
                    <span class="value">${group.currentMembers}/${group.maxMembers}</span>
                </div>
                <div class="detail-row">
                    <span class="label">Dates:</span>
                    <span class="value">${group.startDate} to ${group.endDate}</span>
                </div>
                <div class="detail-row">
                    <span class="label">Base City:</span>
                    <span class="value">${group.baseCity}</span>
                </div>
                ${group.commonDestinations.length > 0 ? `
                <div class="detail-row">
                    <span class="label">Common Destinations:</span>
                    <span class="value">${group.commonDestinations.join(', ')}</span>
                </div>
                ` : ''}
                ${group.commonPreferences.length > 0 ? `
                <div class="detail-row">
                    <span class="label">Common Preferences:</span>
                    <span class="value">${group.commonPreferences.join(', ')}</span>
                </div>
                ` : ''}
            </div>
            <div class="suggestion-actions">
                <button class="btn-primary" onclick="joinExistingGroup('${group.groupId}')">
                    Join This Group
                </button>
            </div>
        </div>
    `).join('');
    
    document.getElementById('suggestions-modal').style.display = 'block';
};

const closeModal = () => {
    document.getElementById('suggestions-modal').style.display = 'none';
};
</script>
```

### 3. CSS Styles
```css
.modal {
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
}

.modal-content {
    background-color: #fefefe;
    margin: 5% auto;
    padding: 0;
    border: none;
    border-radius: 12px;
    width: 80%;
    max-width: 800px;
    max-height: 80vh;
    overflow-y: auto;
}

.modal-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px;
    border-radius: 12px 12px 0 0;
    position: relative;
}

.modal-header h2 {
    margin: 0;
    font-size: 24px;
}

.close {
    position: absolute;
    right: 20px;
    top: 20px;
    font-size: 28px;
    font-weight: bold;
    cursor: pointer;
}

.modal-body {
    padding: 20px;
}

.suggestion-card {
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
    transition: transform 0.2s, box-shadow 0.2s;
}

.suggestion-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.suggestion-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.suggestion-header h3 {
    margin: 0;
    color: #333;
}

.compatibility-badge {
    background: linear-gradient(135deg, #4CAF50, #45a049);
    color: white;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
}

.detail-row {
    display: flex;
    margin-bottom: 8px;
}

.detail-row .label {
    font-weight: bold;
    min-width: 140px;
    color: #666;
}

.detail-row .value {
    color: #333;
}

.suggestion-actions {
    margin-top: 16px;
    text-align: right;
}

.btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    transition: transform 0.2s;
}

.btn-primary:hover {
    transform: translateY(-1px);
}

.btn-secondary {
    background: #f5f5f5;
    color: #333;
    border: 1px solid #ddd;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    margin-right: 10px;
}

.btn-outline {
    background: transparent;
    color: #667eea;
    border: 1px solid #667eea;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
}

.modal-footer {
    padding: 20px;
    text-align: right;
    border-top: 1px solid #e0e0e0;
}

.form-group {
    margin-bottom: 16px;
}

.form-group label {
    display: block;
    margin-bottom: 4px;
    font-weight: bold;
    color: #333;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
}

.form-row {
    display: flex;
    gap: 16px;
}

.form-row .form-group {
    flex: 1;
}

.checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.checkbox-group label {
    display: flex;
    align-items: center;
    font-weight: normal;
    cursor: pointer;
}

.checkbox-group input[type="checkbox"] {
    width: auto;
    margin-right: 8px;
}

.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 16px;
    border-radius: 6px;
    color: white;
    font-weight: bold;
    z-index: 1001;
    animation: slideIn 0.3s ease-out;
}

.notification.success {
    background-color: #4CAF50;
}

.notification.error {
    background-color: #f44336;
}

@keyframes slideIn {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}
```

### 4. Utility Functions
```javascript
// Notification system
const showNotification = (message, type = 'success') => {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 5000);
};

// Form validation
const validateCreateGroupForm = (formData) => {
    const errors = [];
    
    if (!formData.groupName.trim()) {
        errors.push('Group name is required');
    }
    
    if (!formData.tripName.trim()) {
        errors.push('Trip name is required');
    }
    
    if (!formData.startDate || !formData.endDate) {
        errors.push('Start and end dates are required');
    }
    
    if (new Date(formData.startDate) >= new Date(formData.endDate)) {
        errors.push('End date must be after start date');
    }
    
    if (!formData.baseCity.trim()) {
        errors.push('Base city is required');
    }
    
    if (formData.maxMembers < 2 || formData.maxMembers > 20) {
        errors.push('Maximum members must be between 2 and 20');
    }
    
    return errors;
};

// Date formatting
const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

// Current user management (replace with your auth system)
const currentUser = {
    id: 'user123', // Replace with actual user ID from authentication
    name: 'John Doe',
    email: 'john@example.com'
};
```

### 4. Create Group (Original)
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

## Error Handling

### Common Error Scenarios and Handling

```javascript
// Global error handler for pooling service
class PoolingServiceError extends Error {
  constructor(message, status, details) {
    super(message);
    this.name = 'PoolingServiceError';
    this.status = status;
    this.details = details;
  }
}

// Error handling utility
const handlePoolingError = (error, context) => {
  console.error(`Error in ${context}:`, error);
  
  if (error.status === 400) {
    showErrorMessage('Invalid request. Please check your input.');
  } else if (error.status === 404) {
    showErrorMessage('Group not found. It may have been deleted.');
  } else if (error.status === 409) {
    showErrorMessage('You are already a member of this group.');
  } else if (error.status === 500) {
    showErrorMessage('Server error. Please try again later.');
  } else {
    showErrorMessage('An unexpected error occurred. Please try again.');
  }
};

// Enhanced API call with error handling
const makePoolingRequest = async (url, options = {}) => {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      }
    });
    
    if (!response.ok) {
      const errorData = await response.json();
      throw new PoolingServiceError(
        errorData.message || 'Request failed',
        response.status,
        errorData
      );
    }
    
    return await response.json();
  } catch (error) {
    if (error instanceof PoolingServiceError) {
      throw error;
    }
    throw new PoolingServiceError(
      'Network error. Please check your connection.',
      0,
      { originalError: error }
    );
  }
};

// Error-aware versions of key functions
const createGroupWithErrorHandling = async (groupData, tripData, userId) => {
  try {
    const result = await makePoolingRequest('/api/v1/groups/with-trip', {
      method: 'POST',
      body: JSON.stringify({ groupData, tripData, userId })
    });
    
    showSuccessMessage('Group created successfully!');
    return result;
  } catch (error) {
    handlePoolingError(error, 'createGroup');
    throw error;
  }
};

const getTripSuggestionsWithErrorHandling = async (groupId, userId) => {
  try {
    const suggestions = await makePoolingRequest(
      `/api/v1/groups/${groupId}/trip-suggestions?userId=${userId}`
    );
    
    if (!suggestions || suggestions.length === 0) {
      showInfoMessage('No compatible groups found. You can continue with your individual trip.');
    }
    
    return suggestions;
  } catch (error) {
    handlePoolingError(error, 'getTripSuggestions');
    return [];
  }
};

// UI feedback functions
const showErrorMessage = (message) => {
  const errorDiv = document.createElement('div');
  errorDiv.className = 'error-message';
  errorDiv.textContent = message;
  document.body.appendChild(errorDiv);
  
  setTimeout(() => {
    document.body.removeChild(errorDiv);
  }, 5000);
};

const showSuccessMessage = (message) => {
  const successDiv = document.createElement('div');
  successDiv.className = 'success-message';
  successDiv.textContent = message;
  document.body.appendChild(successDiv);
  
  setTimeout(() => {
    document.body.removeChild(successDiv);
  }, 3000);
};

const showInfoMessage = (message) => {
  const infoDiv = document.createElement('div');
  infoDiv.className = 'info-message';
  infoDiv.textContent = message;
  document.body.appendChild(infoDiv);
  
  setTimeout(() => {
    document.body.removeChild(infoDiv);
  }, 4000);
};
```

### Form Validation

```javascript
// Validation utilities
const validateGroupData = (groupData) => {
  const errors = [];
  
  if (!groupData.name || groupData.name.trim().length < 3) {
    errors.push('Group name must be at least 3 characters long');
  }
  
  if (!groupData.budget || !['Low', 'Medium', 'High'].includes(groupData.budget)) {
    errors.push('Please select a valid budget level');
  }
  
  return errors;
};

const validateTripData = (tripData) => {
  const errors = [];
  
  if (!tripData.tripName || tripData.tripName.trim().length < 3) {
    errors.push('Trip name must be at least 3 characters long');
  }
  
  if (!tripData.startDate || !tripData.endDate) {
    errors.push('Please select both start and end dates');
  }
  
  if (new Date(tripData.startDate) >= new Date(tripData.endDate)) {
    errors.push('End date must be after start date');
  }
  
  if (new Date(tripData.startDate) < new Date()) {
    errors.push('Start date cannot be in the past');
  }
  
  if (!tripData.baseCity || tripData.baseCity.trim().length < 2) {
    errors.push('Please enter a valid base city');
  }
  
  return errors;
};

// Form validation with UI feedback
const validateAndSubmitGroupForm = async (event) => {
  event.preventDefault();
  
  const form = event.target;
  const formData = new FormData(form);
  
  const groupData = {
    name: formData.get('groupName'),
    isPublic: formData.get('isPublic') === 'on',
    languages: ['English'],
    interests: Array.from(formData.getAll('interests')),
    budget: formData.get('budget')
  };
  
  const tripData = {
    tripName: formData.get('tripName'),
    startDate: formData.get('startDate'),
    endDate: formData.get('endDate'),
    baseCity: formData.get('baseCity'),
    arrivalTime: formData.get('arrivalTime') || '',
    activityPacing: formData.get('activityPacing') || 'Normal',
    budgetLevel: formData.get('budgetLevel') || 'Medium',
    preferredTerrains: Array.from(formData.getAll('preferredTerrains')),
    preferredActivities: Array.from(formData.getAll('preferredActivities'))
  };
  
  // Validate both group and trip data
  const groupErrors = validateGroupData(groupData);
  const tripErrors = validateTripData(tripData);
  const allErrors = [...groupErrors, ...tripErrors];
  
  if (allErrors.length > 0) {
    showValidationErrors(allErrors);
    return false;
  }
  
  try {
    const result = await createGroupWithErrorHandling(groupData, tripData, currentUserId);
    
    // Check if suggestions are available
    if (result.tripId) {
      await handleTripSuggestions(result.groupId, currentUserId);
    }
    
    return true;
  } catch (error) {
    return false;
  }
};

const showValidationErrors = (errors) => {
  const errorContainer = document.getElementById('validation-errors');
  if (!errorContainer) return;
  
  errorContainer.innerHTML = `
    <div class="validation-error-box">
      <h4>Please correct the following errors:</h4>
      <ul>
        ${errors.map(error => `<li>${error}</li>`).join('')}
      </ul>
    </div>
  `;
  
  errorContainer.scrollIntoView({ behavior: 'smooth' });
};
```

### Loading States and User Feedback

```javascript
// Loading state management
const setLoadingState = (elementId, isLoading) => {
  const element = document.getElementById(elementId);
  if (!element) return;
  
  if (isLoading) {
    element.disabled = true;
    element.innerHTML = '<span class="loading-spinner"></span> Loading...';
  } else {
    element.disabled = false;
    element.innerHTML = element.getAttribute('data-original-text') || 'Submit';
  }
};

// Enhanced group creation with loading state
const createGroupWithLoading = async (groupData, tripData, userId) => {
  const submitButton = document.getElementById('submit-group-form');
  if (submitButton) {
    submitButton.setAttribute('data-original-text', submitButton.innerHTML);
  }
  
  setLoadingState('submit-group-form', true);
  
  try {
    const result = await createGroupWithErrorHandling(groupData, tripData, userId);
    return result;
  } finally {
    setLoadingState('submit-group-form', false);
  }
};

// Progress indicator for multi-step process
const showProgress = (currentStep, totalSteps) => {
  const progressBar = document.getElementById('progress-bar');
  if (!progressBar) return;
  
  const percentage = (currentStep / totalSteps) * 100;
  progressBar.style.width = `${percentage}%`;
  
  const progressText = document.getElementById('progress-text');
  if (progressText) {
    progressText.textContent = `Step ${currentStep} of ${totalSteps}`;
  }
};
```

## Testing

### Unit Tests

```javascript
// Test utilities for pooling service integration
const mockPoolingService = {
  createGroup: jest.fn(),
  getTripSuggestions: jest.fn(),
  finalizeTrip: jest.fn(),
  joinGroup: jest.fn()
};

// Test cases for group creation
describe('Group Creation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  test('should create group with valid data', async () => {
    const groupData = {
      name: 'Test Group',
      isPublic: true,
      languages: ['English'],
      interests: ['Adventure'],
      budget: 'Medium'
    };
    
    const tripData = {
      tripName: 'Test Trip',
      startDate: '2024-08-01',
      endDate: '2024-08-07',
      baseCity: 'Colombo',
      activityPacing: 'Normal',
      budgetLevel: 'Medium'
    };
    
    mockPoolingService.createGroup.mockResolvedValue({
      groupId: 'group-123',
      tripId: 'trip-456',
      message: 'Group created successfully'
    });
    
    const result = await createGroupWithErrorHandling(groupData, tripData, 'user-123');
    
    expect(mockPoolingService.createGroup).toHaveBeenCalledWith({
      groupData,
      tripData,
      userId: 'user-123'
    });
    expect(result.groupId).toBe('group-123');
  });
  
  test('should handle validation errors', async () => {
    const invalidGroupData = {
      name: 'Ab', // Too short
      isPublic: true,
      languages: ['English'],
      interests: ['Adventure'],
      budget: 'Invalid' // Invalid budget
    };
    
    const errors = validateGroupData(invalidGroupData);
    
    expect(errors).toContain('Group name must be at least 3 characters long');
    expect(errors).toContain('Please select a valid budget level');
  });
});

// Test cases for trip suggestions
describe('Trip Suggestions', () => {
  test('should fetch and display trip suggestions', async () => {
    const mockSuggestions = [
      {
        groupId: 'group-789',
        tripName: 'Beach Adventure',
        compatibilityScore: 0.85,
        memberCount: 3,
        baseCity: 'Colombo'
      }
    ];
    
    mockPoolingService.getTripSuggestions.mockResolvedValue(mockSuggestions);
    
    const suggestions = await getTripSuggestionsWithErrorHandling('group-123', 'user-456');
    
    expect(suggestions).toEqual(mockSuggestions);
    expect(mockPoolingService.getTripSuggestions).toHaveBeenCalledWith('group-123', 'user-456');
  });
  
  test('should handle empty suggestions gracefully', async () => {
    mockPoolingService.getTripSuggestions.mockResolvedValue([]);
    
    const suggestions = await getTripSuggestionsWithErrorHandling('group-123', 'user-456');
    
    expect(suggestions).toEqual([]);
  });
});
```

### Integration Tests

```javascript
// Integration test for complete workflow
describe('Complete Pooling Workflow', () => {
  test('should complete full group creation and suggestion flow', async () => {
    // Step 1: Create group with trip
    const groupData = {
      name: 'Adventure Seekers',
      isPublic: true,
      languages: ['English'],
      interests: ['Adventure', 'Nature'],
      budget: 'Medium'
    };
    
    const tripData = {
      tripName: 'Sri Lanka Explorer',
      startDate: '2024-08-01',
      endDate: '2024-08-07',
      baseCity: 'Colombo',
      activityPacing: 'Normal',
      budgetLevel: 'Medium',
      preferredTerrains: ['Beach', 'Mountain'],
      preferredActivities: ['Hiking', 'Photography']
    };
    
    const createResult = await createGroupWithErrorHandling(groupData, tripData, 'user-123');
    expect(createResult.groupId).toBeDefined();
    expect(createResult.tripId).toBeDefined();
    
    // Step 2: Get trip suggestions
    const suggestions = await getTripSuggestionsWithErrorHandling(createResult.groupId, 'user-123');
    expect(Array.isArray(suggestions)).toBe(true);
    
    // Step 3: Handle user decision
    if (suggestions.length > 0) {
      const decision = await handleTripDecision(createResult.groupId, 'user-123', 'join', suggestions[0].groupId);
      expect(decision.success).toBe(true);
    } else {
      const decision = await handleTripDecision(createResult.groupId, 'user-123', 'continue', null);
      expect(decision.success).toBe(true);
    }
  });
});
```

### Manual Testing Scenarios

```javascript
// Manual testing checklist
const testScenarios = [
  {
    name: 'Group Creation Flow',
    steps: [
      '1. Fill out group creation form with valid data',
      '2. Submit form and verify loading state',
      '3. Check success message appears',
      '4. Verify group appears in user\'s groups list'
    ]
  },
  {
    name: 'Trip Suggestions Flow',
    steps: [
      '1. Create a group with specific preferences',
      '2. Wait for suggestions modal to appear',
      '3. Review suggested groups and compatibility scores',
      '4. Test both "Join Group" and "Continue Alone" options'
    ]
  },
  {
    name: 'Error Handling',
    steps: [
      '1. Submit form with invalid data',
      '2. Verify validation errors appear',
      '3. Test network error scenarios',
      '4. Check error messages are user-friendly'
    ]
  },
  {
    name: 'Mobile Responsiveness',
    steps: [
      '1. Test group creation form on mobile',
      '2. Verify suggestions modal works on small screens',
      '3. Check button sizes and touch targets',
      '4. Test form validation on mobile'
    ]
  }
];

// Test data for manual testing
const testData = {
  validGroupData: {
    name: 'Test Adventure Group',
    isPublic: true,
    languages: ['English'],
    interests: ['Adventure', 'Culture'],
    budget: 'Medium'
  },
  validTripData: {
    tripName: 'Sri Lanka Discovery',
    startDate: '2024-08-15',
    endDate: '2024-08-22',
    baseCity: 'Colombo',
    activityPacing: 'Normal',
    budgetLevel: 'Medium',
    preferredTerrains: ['Beach', 'Mountain'],
    preferredActivities: ['Hiking', 'Photography']
  },
  invalidData: {
    shortName: 'AB',
    pastDate: '2023-01-01',
    invalidBudget: 'VeryHigh'
  }
};
```

---

## 🔗 External Service Integrations

The Pooling Service integrates with external microservices to provide enhanced functionality:

### Trip Planning Service Integration

**Purpose**: Fetches detailed trip information including cities, attractions, dates, and preferences

**Key Features**:
- Retrieves complete trip plans with daily itineraries
- Gets user-selected attractions and places for group display
- Supports trip modification operations (city updates, place additions)

**Example Trip Data Retrieved**:
```javascript
// Data structure from Trip Planning Service
const tripDetails = {
  tripId: "trip_001",
  tripName: "Sri Lanka Adventure", 
  startDate: "2025-08-10",
  endDate: "2025-08-15",
  baseCity: "Colombo",
  budgetLevel: "Medium",
  activityPacing: "Normal",
  preferredActivities: ["Hiking", "Cultural Tours"],
  preferredTerrains: ["Beach", "Mountain"],
  cities: ["Colombo", "Kandy", "Ella"],
  topAttractions: ["Sigiriya Rock", "Temple of the Tooth", "Nine Arch Bridge"]
};
```

### User Service Integration

**Purpose**: Resolves user emails to display names for group creators

**Key Features**:
- Fetches user profile information by email
- Provides full names for enhanced group display
- Falls back to email if name is not available

**Example User Data Retrieved**:
```javascript
// Data structure from User Service
const userProfile = {
  email: "john.doe@example.com",
  firstName: "John",
  lastName: "Doe",
  nationality: "American"
};
```

### Frontend Considerations

**Trip Planning Integration**:
- Trip data is automatically fetched when groups are displayed
- No additional frontend calls needed - handled by pooling service backend
- Enhanced group responses include trip details seamlessly

**User Service Integration**:
- Creator names are resolved using stored email addresses
- Frontend should pass user email in group creation requests
- Display names enhance user experience in group listings

**Error Handling**:
- External service failures gracefully degrade functionality
- Basic group information always available even if trip details fail
- Fallback displays maintain usable interface
```

This comprehensive guide provides all the necessary JavaScript code, error handling, validation, and testing scenarios for integrating with the Pooling Service endpoints in your frontend application. The implementation includes proper error handling, loading states, form validation, and comprehensive testing strategies to ensure a robust user experience.
