# Frontend Integration Guide - Pooling Service

This document provides comprehensive integration details for frontend developers working with the Pooling Service endpoints, including the new Public Pooling functionality with trip planning integration.

## Base URL
```
http://localhost:8086/api/v1/groups
```

## Authentication
All endpoints require a `userId` parameter. This will be replaced with JWT authentication in the future.

## 🚀 New Public Pooling Workflow

The new public pooling system allows users to:
1. Create a group with trip planning
2. Plan their trip using the trip-planning service
3. Get suggestions for compatible groups
4. Choose to join an existing group or finalize their own

## 📋 Available Endpoints

### 1. Create Group with Trip Planning (NEW)
**Endpoint**: `POST /api/v1/groups/with-trip`

**Purpose**: Creates a public pooling group with trip planning in one step

**Request Schema**:
```typescript
interface CreateGroupWithTripRequest {
    // Required fields
    userId: string;
    groupName: string;
    tripName: string;
    startDate: string;           // YYYY-MM-DD format
    endDate: string;             // YYYY-MM-DD format
    baseCity: string;
    
    // Optional fields
    arrivalTime?: string;        // HH:mm format or empty
    multiCityAllowed?: boolean;  // Default: true
    activityPacing?: "Relaxed" | "Normal" | "Fast";  // Default: "Normal"
    budgetLevel?: "Low" | "Medium" | "High";         // Default: "Medium"
    preferredTerrains?: string[];
    preferredActivities?: string[];
    visibility?: "private" | "public";  // Default: "public"
    maxMembers?: number;         // Default: 6, range: 2-20
    requiresApproval?: boolean;  // Default: false
    additionalPreferences?: object;
}
```

**Response Schema**:
```typescript
interface CreateGroupWithTripResponse {
    status: "success" | "error";
    groupId: string;
    tripId: string;
    message: string;
    isDraft: boolean;  // Always true for new groups
}
```

**Frontend Implementation**:
```javascript
const createGroupWithTrip = async (groupData) => {
    try {
        const response = await fetch('/api/v1/groups/with-trip', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: groupData.userId,
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
                visibility: "public",
                maxMembers: groupData.maxMembers || 6,
                requiresApproval: false,
                additionalPreferences: groupData.additionalPreferences || {}
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Group and trip created successfully
            console.log('Group created:', result);
            
            // Redirect to trip planning interface
            window.location.href = `/trip-planning/${result.tripId}?groupId=${result.groupId}`;
            
            return {
                success: true,
                groupId: result.groupId,
                tripId: result.tripId,
                message: result.message
            };
        } else {
            throw new Error(result.message || 'Failed to create group');
        }
    } catch (error) {
        console.error('Error creating group with trip:', error);
        return {
            success: false,
            error: error.message
        };
    }
};

// Usage Example
const handleCreatePublicPoolingGroup = async (formData) => {
    const result = await createGroupWithTrip({
        userId: currentUser.id,
        groupName: formData.groupName,
        tripName: formData.tripName,
        startDate: formData.startDate,
        endDate: formData.endDate,
        baseCity: formData.baseCity,
        arrivalTime: formData.arrivalTime,
        activityPacing: formData.activityPacing,
        budgetLevel: formData.budgetLevel,
        preferredTerrains: formData.selectedTerrains,
        preferredActivities: formData.selectedActivities,
        maxMembers: formData.maxMembers
    });
    
    if (result.success) {
        // Show success message
        showNotification('Group created successfully! You can now plan your trip.', 'success');
    } else {
        // Show error message
        showNotification(result.error, 'error');
    }
};
```

### 2. Get Trip Suggestions (NEW)
**Endpoint**: `GET /api/v1/groups/{groupId}/trip-suggestions?userId={userId}`

**Purpose**: Gets compatible group suggestions based on trip similarity

**Response Schema**:
```typescript
interface TripSuggestionsResponse {
    status: "success" | "error";
    groupId: string;
    tripId: string;
    suggestions: CompatibleGroup[];
    message: string;
}

interface CompatibleGroup {
    groupId: string;
    groupName: string;
    tripId: string;
    tripName: string;
    compatibilityScore: number;    // 0-1 scale
    currentMembers: number;
    maxMembers: number;
    commonDestinations: string[];
    commonPreferences: string[];
    createdBy: string;
    startDate: string;
    endDate: string;
    baseCity: string;
}
```

**Frontend Implementation**:
```javascript
const getTripSuggestions = async (groupId, userId) => {
    try {
        const response = await fetch(`/api/v1/groups/${groupId}/trip-suggestions?userId=${userId}`);
        const result = await response.json();
        
        if (response.ok) {
            return {
                success: true,
                suggestions: result.suggestions,
                message: result.message
            };
        } else {
            throw new Error(result.message || 'Failed to get suggestions');
        }
    } catch (error) {
        console.error('Error getting trip suggestions:', error);
        return {
            success: false,
            error: error.message
        };
    }
};

// Usage Example - Call this when user clicks "Save Trip" or "Finalize"
const handleGetSuggestions = async (groupId) => {
    const result = await getTripSuggestions(groupId, currentUser.id);
    
    if (result.success) {
        if (result.suggestions.length > 0) {
            // Show suggestions modal
            showSuggestionsModal(result.suggestions);
        } else {
            // No suggestions found, proceed with finalization
            showConfirmationModal('No similar groups found. Proceed with your trip?');
        }
    } else {
        showNotification(result.error, 'error');
    }
};

// Suggestions Modal Component
const showSuggestionsModal = (suggestions) => {
    const modalContent = `
        <div class="suggestions-modal">
            <h3>We found ${suggestions.length} similar group(s)!</h3>
            <p>You can join an existing group or continue with your own trip.</p>
            
            <div class="suggestions-list">
                ${suggestions.map(group => `
                    <div class="suggestion-card" data-group-id="${group.groupId}">
                        <div class="suggestion-header">
                            <h4>${group.tripName}</h4>
                            <span class="compatibility-score">${Math.round(group.compatibilityScore * 100)}% match</span>
                        </div>
                        <div class="suggestion-details">
                            <p><strong>Group:</strong> ${group.groupName}</p>
                            <p><strong>Members:</strong> ${group.currentMembers}/${group.maxMembers}</p>
                            <p><strong>Dates:</strong> ${group.startDate} to ${group.endDate}</p>
                            <p><strong>Base City:</strong> ${group.baseCity}</p>
                            <p><strong>Common Destinations:</strong> ${group.commonDestinations.join(', ')}</p>
                            <p><strong>Common Preferences:</strong> ${group.commonPreferences.join(', ')}</p>
                        </div>
                        <button class="join-group-btn" onclick="joinExistingGroup('${group.groupId}')">
                            Join This Group
                        </button>
                    </div>
                `).join('')}
            </div>
            
            <div class="modal-actions">
                <button class="btn-secondary" onclick="proceedWithOwnTrip()">
                    Continue with My Trip
                </button>
                <button class="btn-primary" onclick="closeModal()">
                    Let Me Think
                </button>
            </div>
        </div>
    `;
    
    // Display modal with the content
    document.getElementById('modal-container').innerHTML = modalContent;
    document.getElementById('modal-container').style.display = 'block';
};
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
        groupName: formData.groupName,
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
        <label for="groupName">Group Name</label>
        <input type="text" id="groupName" name="groupName" required>
    </div>
    
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
                    <span class="label">Group:</span>
                    <span class="value">${group.groupName}</span>
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

This comprehensive guide provides all the necessary JavaScript code, error handling, validation, and testing scenarios for integrating with the Pooling Service endpoints in your frontend application. The implementation includes proper error handling, loading states, form validation, and comprehensive testing strategies to ensure a robust user experience.
