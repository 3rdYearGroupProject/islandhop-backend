# IslandHop Pooling Service - Frontend Integration Guide

## Service Overview
The IslandHop Pooling Service provides comprehensive group and trip management functionality with two main controllers:

### Base URL
- **Development**: `http://localhost:8086/api/v1`
- **Environment Variable**: `REACT_APP_API_BASE_URL_POOLING_SERVICE`

---

## 🏊‍♂️ Public Pooling Controller (`/api/v1/public-pooling`)

### 1. Pre-check Compatible Groups
**Endpoint**: `POST /public-pooling/pre-check`
**Purpose**: Check for compatible public groups before creating a new one

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: Current user ID
  "baseCity": "string",                  // Required: Starting city
  "startDate": "YYYY-MM-DD",            // Required: Trip start date
  "endDate": "YYYY-MM-DD",              // Required: Trip end date
  "budgetLevel": "Low|Medium|High",     // Required: Budget preference
  "preferredActivities": ["string"],    // Optional: Array of activities
  "preferredTerrains": ["string"],      // Optional: Array of terrains
  "activityPacing": "Relaxed|Normal|Fast", // Optional: Activity pacing
  "multiCityAllowed": boolean           // Required: Multi-city preference
}
```

#### Frontend Implementation:
```javascript
// Using PoolsApi.preCheckCompatibleGroups()
const compatibleGroups = await PoolsApi.preCheckCompatibleGroups({
  userId: currentUser.id,
  baseCity: "Colombo",
  startDate: "2025-10-15",
  endDate: "2025-10-20",
  budgetLevel: "Medium",
  preferredActivities: ["Cultural Tours", "Beach Activities"],
  preferredTerrains: ["Beach", "Cultural Sites"],
  activityPacing: "Normal",
  multiCityAllowed: true
});

// Response structure
{
  "status": "success",
  "message": "string",
  "compatibleGroups": [
    {
      "groupId": "string",
      "tripName": "string",
      "compatibilityScore": 0.85,
      "memberCount": 3,
      "maxMembers": 8,
      "startDate": "2025-10-15",
      "endDate": "2025-10-20",
      "baseCity": "Colombo",
      "highlights": ["Temple of Tooth", "Galle Fort"]
    }
  ],
  "totalCompatibleGroups": 5
}
```

### 2. Save Trip and Get Suggestions
**Endpoint**: `POST /public-pooling/groups/{groupId}/save-trip`
**Purpose**: Save completed trip plan and get similar group suggestions

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: Current user ID
  "tripId": "string",                    // Required: Trip ID from trip planning
  "tripData": {                          // Required: Complete trip data
    "tripName": "string",
    "startDate": "YYYY-MM-DD",
    "endDate": "YYYY-MM-DD",
    "baseCity": "string",
    "cities": ["string"],                // Array of cities to visit
    "destinations": ["string"],          // Array of destinations
    "places": [                          // Array of selected places
      {
        "name": "string",
        "city": "string",
        "type": "Cultural|Beach|Adventure",
        "day": 1
      }
    ],
    "activities": ["string"],            // Selected activities
    "terrains": ["string"],              // Selected terrains
    "itinerary": [                       // Day-by-day itinerary
      {
        "day": 1,
        "date": "YYYY-MM-DD",
        "city": "string",
        "activities": ["string"]
      }
    ]
  },
  "optionalField": "string"              // Optional: Additional metadata
}
```

#### Frontend Implementation:
```javascript
// Using PoolsApi.saveTripAndGetSuggestions()
const result = await PoolsApi.saveTripAndGetSuggestions(groupId, {
  userId: currentUser.id,
  tripId: tripPlanningResult.tripId,
  tripData: {
    tripName: "Amazing Sri Lanka Journey",
    startDate: "2025-10-15",
    endDate: "2025-10-20",
    baseCity: "Colombo",
    cities: ["Colombo", "Kandy", "Galle"],
    destinations: ["Colombo", "Kandy", "Galle"],
    places: completedItinerary.places,
    activities: selectedActivities,
    terrains: selectedTerrains,
    itinerary: dailyItinerary
  }
});

// Response: Similar groups or confirmation to proceed
if (result.suggestions && result.suggestions.length > 0) {
  // Show user similar groups option
  setShowSuggestions(true);
  setSuggestedGroups(result.suggestions);
} else {
  // No similar groups, proceed to finalize
  finalizeNewGroup();
}
```

### 3. Join Existing Group
**Endpoint**: `POST /public-pooling/groups/{groupId}/join-existing`
**Purpose**: Join a suggested group and discard the new group

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: Current user ID
  "targetGroupId": "string",             // Required: ID of group to join
  "message": "string"                    // Required: Message to group members
}
```

#### Frontend Implementation:
```javascript
// When user chooses to join suggested group
const joinResult = await PoolsApi.joinExistingGroup(currentGroupId, {
  userId: currentUser.id,
  targetGroupId: selectedSuggestion.groupId,
  message: "I'd like to join your group instead of creating my own!"
});

// Navigate to joined group
if (joinResult.status === 'success') {
  navigate(`/groups/${selectedSuggestion.groupId}`);
}
```

### 4. Finalize Group
**Endpoint**: `POST /public-pooling/groups/{groupId}/finalize`
**Purpose**: Finalize and activate the new group

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: Current user ID
  "action": "finalize|cancel",           // Required: Action to take
  "reason": "string"                     // Optional: Reason for action
}
```

#### Frontend Implementation:
```javascript
// When user chooses to keep their new group
const finalizeResult = await PoolsApi.finalizeGroup(groupId, {
  userId: currentUser.id,
  action: "finalize",
  reason: "Proceeding with our planned group trip"
});

// Navigate to finalized group
if (finalizeResult.status === 'success') {
  navigate(`/groups/${groupId}`);
}
```

### 5. Get Compatible Groups for Trip
**Endpoint**: `GET /public-pooling/groups/compatible/{tripId}?userId={userId}`
**Purpose**: Get compatible groups for an existing trip

#### Frontend Implementation:
```javascript
const compatibleGroups = await PoolsApi.getCompatibleGroups(tripId, userId);
```

### 6. Get Comprehensive Trip Details
**Endpoint**: `GET /public-pooling/trips/{tripId}/comprehensive?userId={userId}`
**Purpose**: Get complete trip details including itinerary and members

#### Frontend Implementation:
```javascript
// Get comprehensive trip details (works for both logged-in and anonymous users)
const tripDetails = await PoolsApi.getComprehensiveTripDetails(tripId, userId);

// Response structure
{
  "tripDetails": {
    "tripId": "string",
    "tripName": "string",
    "startDate": "YYYY-MM-DD",
    "endDate": "YYYY-MM-DD",
    "baseCity": "string",
    "dailyPlans": [
      {
        "day": 1,
        "city": "string",
        "attractions": [/*place objects*/],
        "restaurants": [/*place objects*/],
        "hotels": [/*place objects*/]
      }
    ]
  },
  "groupInfo": {
    "groupId": "string",
    "groupName": "string",
    "visibility": "public|private",
    "status": "active|finalized",
    "maxMembers": 8,
    "creatorName": "string"
  },
  "members": [
    {
      "userId": "string",
      "userName": "string",
      "joinedAt": "ISO date"
    }
  ],
  "statistics": {
    "totalDays": 5,
    "totalCities": 3,
    "totalAttractions": 12
  }
}
```

---

## 🏠 Group Controller (`/api/v1/groups`)

### 1. Create Group with Trip
**Endpoint**: `POST /groups/with-trip`
**Purpose**: Create a new group with integrated trip planning

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: Creator user ID
  "userEmail": "string",                 // Optional: Creator email
  "groupName": "string",                 // Required: Group name
  "tripName": "string",                  // Required: Trip name
  "startDate": "YYYY-MM-DD",            // Required: Trip start date
  "endDate": "YYYY-MM-DD",              // Required: Trip end date
  "baseCity": "string",                  // Required: Starting city
  "arrivalTime": "HH:mm",               // Optional: Arrival time
  "multiCityAllowed": boolean,           // Optional: Default true
  "activityPacing": "Relaxed|Normal|Fast", // Optional: Default "Normal"
  "budgetLevel": "Low|Medium|High",     // Optional: Default "Medium"
  "preferredTerrains": ["string"],      // Optional: Default []
  "preferredActivities": ["string"],    // Optional: Default []
  "visibility": "public|private",        // Optional: Group visibility
  "maxMembers": number,                  // Optional: Max group size
  "requiresApproval": boolean,           // Optional: Require approval to join
  "additionalPreferences": {             // Optional: Additional preferences
    "accommodationType": "string",
    "transportPreference": "string",
    "dietaryRequirements": "string"
  }
}
```

#### Frontend Implementation:
```javascript
// Using PoolsApi.createGroupWithTrip()
const newGroup = await PoolsApi.createGroupWithTrip({
  userId: currentUser.id,
  userEmail: currentUser.email,
  groupName: "Sri Lanka Adventure Group",
  tripName: "Amazing Sri Lanka Journey",
  startDate: "2025-10-15",
  endDate: "2025-10-20",
  baseCity: "Colombo",
  visibility: "public",
  maxMembers: 8,
  requiresApproval: true,
  budgetLevel: "Medium",
  preferredActivities: ["Cultural Tours", "Beach Activities"],
  additionalPreferences: {
    accommodationType: "mid-range hotels",
    transportPreference: "private vehicle"
  }
});

// Navigate to trip planning with the created group
navigate(`/trip-planning/${newGroup.tripId}?groupId=${newGroup.groupId}`);
```

### 2. Get Enhanced Public Groups
**Endpoint**: `GET /groups/public/enhanced`
**Purpose**: Browse all public groups with detailed information

#### Query Parameters:
- `userId` (optional): Current user ID for compatibility scoring
- `baseCity` (optional): Filter by base city
- `startDate` (optional): Filter by start date
- `endDate` (optional): Filter by end date
- `budgetLevel` (optional): Filter by budget level
- `preferredActivities` (optional): Filter by activities (can be multiple)

#### Frontend Implementation:
```javascript
// Using PoolsApi.getEnhancedPools() (for Find Pools page)
const publicPools = await PoolsApi.getEnhancedPools({
  userId: currentUser?.id,
  baseCity: filters.city,
  budgetLevel: filters.budget,
  preferredActivities: filters.activities
});

// Convert to frontend format
const poolsForDisplay = publicPools.map(group => PoolsApi.convertToPoolFormat(group));
```

### 3. Get User's Groups (Created + Participant)
**Endpoint**: `GET /groups/created-by/{userId}`
**Purpose**: Get all groups where the user is involved (both created groups and groups where user is a participant)

#### Frontend Implementation:
```javascript
// Using PoolsApi.getUserCreatedPools() (for My Pools page)
// Note: This now returns BOTH created groups AND groups where user is a participant
const userPools = await PoolsApi.getUserPools(currentUser.id);

// Returns organized pools (created + participant)
{
  "upcoming": [/*pool objects - includes created and joined pools*/],
  "ongoing": [/*pool objects - includes created and joined pools*/],
  "past": [/*pool objects - includes created and joined pools*/]
}

// Each pool object includes additional context
{
  "id": "group123",
  "groupName": "Sri Lanka Adventure",
  "creatorUserId": "creator789",  // Can be different from current user
  "userIds": ["creator789", "user456", "currentUser123"], // All participants
  "isCreator": true/false,        // Whether current user is the creator
  // ... other group fields
}
```

#### Use Cases:
- **My Groups Dashboard**: Shows all groups user is involved in
- **Group Management**: User can manage created groups differently from joined groups
- **Unified Experience**: Single endpoint for complete user group overview

### 4. Join Group
**Endpoint**: `POST /groups/{groupId}/join`
**Purpose**: Request to join a public group

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: User requesting to join
  "userEmail": "string",                 // Optional: User email
  "userName": "string",                  // Optional: User display name
  "message": "string",                   // Required: Personal message
  "userProfile": {                       // Optional: User profile info
    "age": number,
    "interests": ["string"],
    "travelExperience": "string",
    "languages": ["string"]
  }
}
```

#### Frontend Implementation:
```javascript
// Using PoolsApi.joinPool()
const joinResult = await PoolsApi.joinPool(groupId, {
  userId: currentUser.id,
  userEmail: currentUser.email,
  userName: currentUser.name,
  message: "I'm excited to join your Sri Lanka adventure!",
  userProfile: {
    age: 28,
    interests: ["Photography", "Cultural Tours"],
    travelExperience: "Experienced",
    languages: ["English", "Spanish"]
  }
});

// Handle different join scenarios
if (joinResult.status === 'pending') {
  showMessage("Join request sent! Group members will vote on your request.");
} else if (joinResult.status === 'success') {
  showMessage("Welcome to the group!");
  navigate(`/groups/${groupId}`);
}
```

### 5. Invite User to Group
**Endpoint**: `POST /groups/{groupId}/invite`
**Purpose**: Invite a user to a private group

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: User sending invitation
  "invitedEmail": "string",              // Optional: Email to invite
  "invitedUserId": "string",             // Optional: User ID to invite
  "message": "string",                   // Required: Invitation message
  "expirationDays": number               // Optional: Default 7 days
}
```

#### Frontend Implementation:
```javascript
const inviteResult = await PoolsApi.inviteUserToGroup(groupId, {
  userId: currentUser.id,
  invitedEmail: "friend@example.com",
  message: "Join our amazing Sri Lanka trip!",
  expirationDays: 7
});
```

### 6. Get User Invitations
**Endpoint**: `GET /groups/invitations/{userId}`
**Purpose**: Get all invitations for a user

#### Frontend Implementation:
```javascript
// Using PoolsApi.getUserInvitations()
const invitations = await PoolsApi.getUserInvitations(currentUser.id);

// Response structure
{
  "invitations": [
    {
      "invitationId": "string",
      "groupId": "string",
      "groupName": "string",
      "tripName": "string",
      "inviterName": "string",
      "message": "string",
      "expiresAt": "ISO date",
      "status": "pending|accepted|rejected"
    }
  ],
  "totalInvitations": 3
}
```

### 7. Respond to Invitation
**Endpoint**: `POST /groups/invitations/respond`
**Purpose**: Accept or reject an invitation

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: User responding
  "invitationId": "string",              // Required: Invitation ID
  "action": "accept|reject",             // Required: Response action
  "message": "string"                    // Optional: Response message
}
```

#### Frontend Implementation:
```javascript
// Using PoolsApi.respondToInvitation()
const response = await PoolsApi.respondToInvitation({
  userId: currentUser.id,
  invitationId: invitation.id,
  action: "accept",
  message: "Excited to join the trip!"
});
```

### 8. Get Pending Join Requests
**Endpoint**: `GET /groups/{groupId}/join-requests/pending?userId={userId}`
**Purpose**: Get pending join requests for a group (for admins/members)

#### Frontend Implementation:
```javascript
const pendingRequests = await PoolsApi.getPendingJoinRequests(groupId, currentUser.id);
```

### 9. Get All Pending Requests for User
**Endpoint**: `GET /groups/my-pending-requests?userId={userId}`
**Purpose**: Get all pending requests across groups where user is a member

#### Frontend Implementation:
```javascript
// Using PoolsApi.getMyPendingRequests()
const allPendingRequests = await PoolsApi.getMyPendingRequests(currentUser.id);

// Response structure
{
  "groups": [
    {
      "groupInfo": {
        "groupId": "string",
        "groupName": "string",
        "tripName": "string"
      },
      "pendingRequests": [
        {
          "requestUserId": "string",
          "userName": "string",
          "message": "string",
          "requestedAt": "ISO date",
          "userVoteStatus": "not_voted|approved|rejected"
        }
      ]
    }
  ],
  "totalGroups": 2,
  "totalPendingRequests": 5
}
```

### 10. Vote on Join Request
**Endpoint**: `POST /groups/{groupId}/join-requests/{requestUserId}/vote`
**Purpose**: Vote on a join request (approve/reject)

#### Request Body:
```javascript
{
  "userId": "string",                    // Required: User voting
  "approved": boolean,                   // Required: true=approve, false=reject
  "comment": "string"                    // Optional: Vote comment
}
```

#### Frontend Implementation:
```javascript
// Using PoolsApi.voteOnJoinRequestNew()
const voteResult = await PoolsApi.voteOnJoinRequestNew(groupId, requestUserId, {
  userId: currentUser.id,
  approved: true,
  comment: "Welcome to our group!"
});

// Response indicates if request is now approved/rejected
{
  "status": "success",
  "requestStatus": "approved|rejected|pending",
  "totalVotesReceived": 3,
  "totalMembersRequired": 4,
  "message": "Vote recorded successfully"
}
```

---

## 🎯 Common Frontend Usage Patterns

### 1. Complete Public Pooling Flow
```javascript
// 1. Pre-check for compatible groups
const preCheck = await PoolsApi.preCheckCompatibleGroups(tripPreferences);

if (preCheck.compatibleGroups.length > 0) {
  // Show existing options to user
  setCompatibleGroups(preCheck.compatibleGroups);
}

// 2. If user chooses to create new group
const newGroup = await PoolsApi.createGroupWithTrip(groupData);

// 3. User completes trip planning
// ... trip planning flow ...

// 4. Save trip and get suggestions
const suggestions = await PoolsApi.saveTripAndGetSuggestions(groupId, tripData);

if (suggestions.suggestions?.length > 0) {
  // Show similar groups to user
  setSimilarGroups(suggestions.suggestions);
} else {
  // No similar groups, proceed to finalize
  await PoolsApi.finalizeGroup(groupId, { userId, action: "finalize" });
}
```

### 2. Browse and Join Groups
```javascript
// Browse available public groups
const publicGroups = await PoolsApi.getEnhancedPools(filters);
const poolsForDisplay = publicGroups.map(PoolsApi.convertToPoolFormat);

// Join a selected group
const joinResult = await PoolsApi.joinPool(selectedGroupId, joinData);

// Handle voting if required
if (joinResult.requiresVoting) {
  showMessage("Your request is pending member approval");
}
```

### 3. Manage User's Groups
```javascript
// Get user's created groups
const userPools = await PoolsApi.getUserPools(currentUser.id);

// Get invitations
const invitations = await PoolsApi.getUserInvitations(currentUser.id);

// Get pending requests to vote on
const pendingVotes = await PoolsApi.getMyPendingRequests(currentUser.id);
```

---

## 🔧 Error Handling

All endpoints follow consistent error response format:
```javascript
{
  "status": "error",
  "message": "Error description",
  "errors": {                    // For validation errors
    "fieldName": "Field error message"
  }
}
```

### Common HTTP Status Codes:
- `200` - Success
- `201` - Created (for group/trip creation)
- `400` - Bad Request (validation errors)
- `403` - Forbidden (unauthorized access)
- `404` - Not Found (group/trip not found)
- `500` - Internal Server Error

### Frontend Error Handling:
```javascript
try {
  const result = await PoolsApi.someMethod(data);
  // Handle success
} catch (error) {
  if (error.message.includes('not found')) {
    showError('Group not found');
  } else if (error.message.includes('unauthorized')) {
    showError('Access denied');
  } else {
    showError('Something went wrong. Please try again.');
  }
}
```

---

## 🧪 Testing with Postman

Import the complete test collection: `POOLING_SERVICE_COMPLETE_TEST_COLLECTION.postman_collection.json`

### Environment Variables:
- `base_url`: `http://localhost:8086/api/v1`
- `user_id`: Test user ID
- `friend_user_id`: Second test user ID
- `user_email`: Test user email

### Test Scenarios Included:
1. **Complete Public Pooling Flow**: Pre-check → Create → Save Trip → Finalize
2. **Private Group Management**: Create → Invite → Accept → Manage
3. **Group Discovery**: Browse → Filter → Join → Vote
4. **Trip Details**: Comprehensive details → Anonymous access
5. **Error Scenarios**: Invalid data → Unauthorized access → Not found

The collection includes comprehensive test assertions and automatically manages test data flow between requests.
